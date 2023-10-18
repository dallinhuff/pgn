package com.dallinhuff.pgn.model

import cats.data.NonEmptyList

object Model:
  
  // all possible files on the board
  enum File:
    case A, B, C, D, E, F, G, H
    
  // all possible ranks on the board
  enum Rank:
    case One, Two, Three, Four, Five, Six, Seven, Eight
    
  // all possible squares on the board
  final case class Square(file: File, rank: Rank)

  // all possible "starts" a PGN move may include in order
  // to disambiguate an otherwise ambiguous move
  enum Start:
    case FileStart(file: File)
    case RankStart(rank: Rank)
    case SquareStart(square: Square)
    case Unspecified
  
  // all possible (non-pawn) pieces
  enum Piece:
    case King, Queen, Rook, Bishop, Knight
  
  // all possible move types
  enum Move:
    case Castle(side: CastleSide)
    case Promotion(pawnMove: Pawn, promotedTo: Piece)
    case Standard(piece: Piece, start: Option[Start], isCapture: Boolean, end: Square, checkStatus: Option[CheckStatus])
    case Pawn(start: Option[Start], isCapture: Boolean, end: Square, checkStatus: Option[CheckStatus])
    
  // all possible castle types
  enum CastleSide:
    case KingSide, QueenSide
    
  // all possible check states
  enum CheckStatus:
    case Check, Checkmate, Neither

  // all possible PGN turns
  enum Turn:
    case Full(white: Move, black: Move)
    case Partial(white: Move)

  // all possible game outcomes
  enum Outcome:
    case WhiteWins, BlackWins, Draw, Unknown

  // a PGN game - a sequence of one or more turns and an outcome
  final case class Game(turns: NonEmptyList[Turn], outcome: Outcome)