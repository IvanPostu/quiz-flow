import { API_BASE_URL } from "src/constants/constants";
import { handleAndThrowIfNeeded } from "../utils/requestErrorHandler";
import { Quiz, QuizAnswer, QuizQuestion } from "./Quiz";
import { parsePreciseISO } from "../utils/parsePreciseISO";

export async function getQuiz(
  accessToken: string,
  quizId: string
): Promise<Quiz> {
  const url = `${API_BASE_URL}/api/quizzes/${quizId}`;
  const res = await fetch(url, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer " + accessToken,
    },
  });

  await handleAndThrowIfNeeded(res);
  const data: QuizResponse = (await res.json()) as QuizResponse;
  return mapQuizResponseToQuiz(data);
}

export async function createQuiz(
  accessToken: string,
  request: QuizCreateRequest
): Promise<Quiz> {
  const url = `${API_BASE_URL}/api/quizzes`;
  const res = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer " + accessToken,
    },
    body: JSON.stringify(request),
  });

  await handleAndThrowIfNeeded(res);
  const data: QuizResponse = (await res.json()) as QuizResponse;
  return mapQuizResponseToQuiz(data);
}

export async function updateQuiz(
  accessToken: string,
  quizId: string,
  request: QuizUpdateRequest
): Promise<Quiz> {
  const url = `${API_BASE_URL}/api/quizzes/${quizId}`;
  const res = await fetch(url, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer " + accessToken,
    },
    body: JSON.stringify(request),
  });

  await handleAndThrowIfNeeded(res);
  const data: QuizResponse = (await res.json()) as QuizResponse;
  return mapQuizResponseToQuiz(data);
}

function mapQuizResponseToQuiz(response: QuizResponse): Quiz {
  const questions: QuizQuestion[] = response.questions.map((value) => ({
    question: value.question,
    questionId: value.question_id,
    answerOptions: value.answer_options,
    correctAnswerExplanation: value.correct_answer_explanation,
    correctAnswerIndexes: value.correct_answer_indexes,
  }));
  const answers: QuizAnswer[] = response.answers.map((value) => ({
    questionId: value.question_id,
    chosenAnswerIndexes: value.chosen_answer_indexes,
  }));
  return {
    id: response.id,
    questionSetId: response.question_set_id,
    questionSetVersion: response.question_set_version,
    createdDate: parsePreciseISO(response.created_date),
    finalizedDate: response.finalized_date
      ? parsePreciseISO(response.finalized_date)
      : undefined,
    questions: questions,
    answers: answers,
  };
}

export interface QuizUpdateRequest {
  readonly finalize: boolean;
  readonly answers: QuizAnswerRequest[];
}

export interface QuizAnswerRequest {
  readonly question_id: string;
  readonly chosen_answer_indexes: number[];
}

interface QuizCreateRequest {
  readonly question_set_id: string;
  readonly question_set_version: number;
  readonly question_ids: string[];
}

interface QuizResponse {
  readonly id: string;
  readonly question_set_id: string;
  readonly question_set_version: number;
  readonly created_date: string;
  readonly finalized_date?: string;
  readonly questions: QuizQuestionResponse[];
  readonly answers: QuizAnswerResponse[];
}

interface QuizQuestionResponse {
  readonly question_id: string;
  readonly question: string;
  readonly answer_options: string[];
  readonly correct_answer_indexes: number[];
  readonly correct_answer_explanation: string;
}

interface QuizAnswerResponse {
  readonly question_id: string;
  readonly chosen_answer_indexes: number[];
}
