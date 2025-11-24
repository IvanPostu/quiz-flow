export interface Quiz {
  readonly id: string;
  readonly questionSetId: string;
  readonly questionSetVersion: number;
  readonly createdDate: Date;
  readonly finalizedDate?: Date;
  readonly questions: QuizQuestion[];
  readonly answers: QuizAnswer[];
}

export interface QuizQuestion {
  readonly questionId: string;
  readonly question: string;
  readonly answerOptions: string[];
  readonly  correctAnswerIndexes: number[];
   readonly correctAnswerExplanation: string;
}

export interface QuizAnswer {
  readonly questionId: string;
  readonly chosenAnswerIndexes: number[];
}
