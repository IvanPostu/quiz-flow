import { API_BASE_URL } from "src/constants/constants";
import { handleAndThrowIfNeeded } from "../utils/requestErrorHandler";
import { parsePreciseISO } from "../utils/parsePreciseISO";
import { QuizResult, QuizResultAnswer } from "./QuizResult";

export async function getQuizResult(
  accessToken: string,
  quizId: string
): Promise<QuizResult> {
  const url = `${API_BASE_URL}/api/quiz-results/${quizId}`;
  const res = await fetch(url, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer " + accessToken,
    },
  });

  await handleAndThrowIfNeeded(res);
  const data: QuizResultResponse = (await res.json()) as QuizResultResponse;
  return mapQuizResultResponseToQuizResult(data);
}

export async function getQuizResultList(
  accessToken: string,
  limit: number,
  offset: number,
  sortOrder: "ASC" | "DESC"
): Promise<QuizResult[]> {
  const queryParameters = new URLSearchParams({
    offset: "" + offset,
    limit: "" + limit,
    sortOrder: sortOrder,
  });

  const url = `${API_BASE_URL}/api/quiz-results?${queryParameters.toString()}`;
  const res = await fetch(url, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer " + accessToken,
    },
  });

  await handleAndThrowIfNeeded(res);
  const data: QuizResultResponse[] = (await res.json()) as QuizResultResponse[];
  return data.map((value) => mapQuizResultResponseToQuizResult(value));
}

function mapQuizResultResponseToQuizResult(
  response: QuizResultResponse
): QuizResult {
  const answers: QuizResultAnswer[] = response.answers.map((value) => ({
    questionId: value.question_id,
    chosenAnswerIndexes: value.chosen_answer_indexes,
    rightAnswerIndexes: value.right_answer_indexes,
  }));
  return {
    quizId: response.quiz_id,
    questionSetId: response.question_set_id,
    questionSetName: response.question_set_name,
    questionSetVersion: response.question_set_version,
    quizCreatedDate: parsePreciseISO(response.quiz_created_date),
    quizFinalizedDate: response.quiz_finalized_date
      ? parsePreciseISO(response.quiz_finalized_date)
      : undefined,
    questionCount: response.question_count,
    answersCount: response.answers_count,
    correctAnswersCount: response.correct_answers_count,
    answers: answers,
  };
}

interface QuizResultResponse {
  readonly quiz_id: string;
  readonly question_set_id: string;
  readonly question_set_name: string;
  readonly question_set_version: number;
  readonly quiz_created_date: string;
  readonly quiz_finalized_date?: string;
  readonly question_count: number;
  readonly answers_count: number;
  readonly correct_answers_count?: number;
  readonly answers: QuizResultAnswerResponse[];
}

interface QuizResultAnswerResponse {
  readonly question_id: string;
  readonly chosen_answer_indexes: number[];
  readonly right_answer_indexes?: number[];
}
