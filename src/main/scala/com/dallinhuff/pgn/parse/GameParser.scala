package com.dallinhuff.pgn.parse

import cats.data.NonEmptyList
import cats.parse.{Parser, Parser0}
import cats.parse.Parser.{char, charIn, oneOf, oneOf0, string}
import cats.parse.Rfc5234.{cr, crlf, digit, lf, wsp}
import com.dallinhuff.pgn.model.Model.*

object GameParser:
  def parse(s: String): Either[Parser.Error, Game] =
    (turn.backtrack.rep ~ outcome)
      .map(Game.apply.tupled)
      .parse(s)
      .map(_._2)

  private def parserFromMapping[A](mapping: Map[Char, A]): Parser0[A] =
    charIn(mapping.keys.toList).map(mapping.apply)

  private val file: Parser0[File] =
    parserFromMapping(Map(
      'a' -> File.A,
      'b' -> File.B,
      'c' -> File.C,
      'd' -> File.D,
      'e' -> File.E,
      'f' -> File.F,
      'g' -> File.G,
      'h' -> File.H
    ))

  private val rank: Parser0[Rank] =
    parserFromMapping(Map(
      '1' -> Rank.One,
      '2' -> Rank.Two,
      '3' -> Rank.Three,
      '4' -> Rank.Four,
      '5' -> Rank.Five,
      '6' -> Rank.Six,
      '7' -> Rank.Seven,
      '8' -> Rank.Eight
    ))

  private val square: Parser0[Square] = (file ~ rank).map(Square.apply.tupled)

  private val piece: Parser0[Piece] =
    parserFromMapping(Map(
      'K' -> Piece.King,
      'Q' -> Piece.Queen,
      'R' -> Piece.Rook,
      'B' -> Piece.Bishop,
      'N' -> Piece.Knight
    ))

  private val check: Parser0[CheckStatus] =
    char('+').as(CheckStatus.Check) orElse char('#').as(CheckStatus.Checkmate)

  private val source: Parser0[Start] =
    oneOf0(
      square.map(Start.SquareStart.apply).backtrack ::
      file.map(Start.FileStart.apply) ::
      rank.map(Start.RankStart.apply) ::
      Nil
    )

  private val move: Parser0[Move] =
    val castle: Parser0[Move] =
      string("O-O-O").as(Move.Castle(CastleSide.QueenSide)) orElse
      string("O-O").as(Move.Castle(CastleSide.KingSide))

    val nonPawn: Parser0[Move.Standard] =
      (piece ~ char('x').? ~ square ~ check.?)
        .map { case (((p, capture), end), chk) =>
          Move.Standard(p, None, capture.isDefined, end, chk)
        }

    val nonPawnD: Parser0[Move.Standard] =
      (piece ~ source ~ char('x').? ~ square ~ check.?)
        .map { case ((((p, src), capture), end), chk) =>
          Move.Standard(p, Some(src), capture.isDefined, end, chk)
        }

    val promotion: Parser0[Piece] =
      char('=') *> piece

    val pawn: Parser0[Move] =
      ((file.soft ~ char('x')).? ~ square ~ promotion.? ~ check.?)
        .map { case (((srcCapture, end), promo), checkStatus) =>
          val pMove: Move.Pawn = Move.Pawn(
            srcCapture.map(s => Start.FileStart(s._1)),
            srcCapture.isDefined,
            end,
            checkStatus
          )
          promo.map(Move.Promotion(pMove, _)).getOrElse(pMove)
        }

    oneOf0(castle :: nonPawnD.backtrack :: nonPawn :: pawn :: Nil)

  private val turn: Parser[Turn] =
    val ws = oneOf(wsp :: cr :: crlf :: lf :: Nil).rep
    digit.rep *> char('.') *> ws.rep *> (
      (move ~ (move surroundedBy ws)).map(Turn.Full.apply.tupled).backtrack
      orElse
      (move <* ws.rep).map(Turn.Partial.apply)
    )

  private val outcome: Parser[Outcome] =
    val draw = string("1/2-1/2").as(Outcome.Draw)
    val whiteWins = string("1-0").as(Outcome.WhiteWins)
    val blackWins = string("0-1").as(Outcome.BlackWins)
    val unknown = char('*').as(Outcome.Unknown)
    oneOf(draw :: whiteWins :: blackWins :: unknown :: Nil)