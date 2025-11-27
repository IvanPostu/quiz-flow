export interface QuizResult {
  readonly quizId: string;
  readonly questionSetId: string;
  readonly questionSetVersion: number;
  readonly quizCreatedDate: Date;
  readonly quizFinalizedDate?: Date;
  readonly questionCount: number;
  readonly answersCount: number;
  readonly correctAnswersCount: number;
  readonly answers: QuizResultAnswer[];
}

export interface QuizResultAnswer {
  readonly questionId: string;
  readonly chosenAnswerIndexes: number[];
  readonly rightAnswerIndexes: number[];
}
