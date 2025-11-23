export interface Quiz {
  id: string;
  questionSetId: string;
  questionSetVersion: number;
  createdDate: Date;
  finalizedDate?: Date;
  isFinalized: boolean;
  questions: QuizQuestion[];
  answers: QuizAnswer[];
}

export interface QuizQuestion {
  questionId: string;
  question: string;
  answerOptions: string[];
}

export interface QuizAnswer {
  questionId: string;
  chosenAnswerIndexes: number[];
}
