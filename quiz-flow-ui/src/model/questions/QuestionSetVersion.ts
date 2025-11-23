export interface QuestionSetVersion {
  id: string;
  version: number;
  questions: Question[];
}

export interface Question {
  id: string;
  question: string;
  answerOptions: string[];
  correctAnswerIndexes: string[];
  correctAnswerExplanation: string;
}
