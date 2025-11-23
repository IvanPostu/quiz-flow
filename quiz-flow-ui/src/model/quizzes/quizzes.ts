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
    isFinalized: response.is_finalized,
    questions: questions,
    answers: answers,
  };
}

interface QuizUpdateRequest {
  finalize: boolean;
  answers: QuizAnswerRequest[];
}

interface QuizAnswerRequest {
  question_id: string;
  chosen_answer_indexes: number[];
}

interface QuizCreateRequest {
  question_set_id: string;
  question_set_version: number;
  question_ids: string[];
}

interface QuizResponse {
  id: string;
  question_set_id: string;
  question_set_version: number;
  created_date: string;
  finalized_date?: string;
  is_finalized: boolean;
  questions: QuizQuestionResponse[];
  answers: QuizAnswerResponse[];
}

interface QuizQuestionResponse {
  question_id: string;
  question: string;
  answer_options: string[];
}

interface QuizAnswerResponse {
  question_id: string;
  chosen_answer_indexes: number[];
}
