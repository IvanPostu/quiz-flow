import { API_BASE_URL } from "src/constants/constants";
import { handleAndThrowIfNeeded } from "../utils/requestErrorHandler";
import { Question, QuestionSetVersion } from "./QuestionSetVersion";

export async function getQuestionSetVersion(
  accessToken: string,
  questionSetId: string,
  version: number
): Promise<QuestionSetVersion> {
  const url = `${API_BASE_URL}/api/question-sets/${questionSetId}/questions/versions/${version}`;
  const res = await fetch(url, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer " + accessToken,
    },
  });

  await handleAndThrowIfNeeded(res);
  const data: QuestionSetVersionResponse =
    (await res.json()) as QuestionSetVersionResponse;
  return mapQuestionSetVersionResponseToQuestionSetVersion(data);
}

function mapQuestionSetVersionResponseToQuestionSetVersion(
  response: QuestionSetVersionResponse
): QuestionSetVersion {
  const questions: Question[] = response.questions.map((value) => ({
    id: value.id,
    question: value.question,
    answerOptions: value.answer_options,
    correctAnswerIndexes: value.correct_answer_indexes,
    correctAnswerExplanation: value.correct_answer_explanation,
  }));
  return {
    id: response.id,
    version: response.version,
    questions: questions,
  };
}

interface QuestionSetVersionResponse {
  id: string;
  version: number;
  questions: QuestionResponse[];
}

interface QuestionResponse {
  id: string;
  question: string;
  answer_options: string[];
  correct_answer_indexes: string[];
  correct_answer_explanation: string;
}
