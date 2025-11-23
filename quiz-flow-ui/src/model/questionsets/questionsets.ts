import { API_BASE_URL } from "src/constants/constants";
import { handleAndThrowIfNeeded } from "../utils/requestErrorHandler";
import { QuestionSet } from "./QuestionSet";
import { parsePreciseISO } from "../utils/parsePreciseISO";

export async function list(
  accessToken: string,
  limit: number,
  offset: number,
  sortOrder: "ASC" | "DESC"
): Promise<QuestionSet[]> {
  const queryParameters = new URLSearchParams({
    offset: "" + offset,
    limit: "" + limit,
    sortOrder: sortOrder,
  });

  const res = await fetch(
    `${API_BASE_URL}/api/question-sets?${queryParameters.toString()}`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + accessToken,
      },
    }
  );

  await handleAndThrowIfNeeded(res);
  const data: QuestionSetResponse[] =
    (await res.json()) as QuestionSetResponse[];
  return data.map((value) => mapQuestionSetResponseToQuestionSet(value));
}

export async function getQuestionSet(
  accessToken: string,
  id: string
): Promise<QuestionSet> {
  const res = await fetch(`${API_BASE_URL}/api/question-sets/${id}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer " + accessToken,
    },
  });

  await handleAndThrowIfNeeded(res);
  const data: QuestionSetResponse = (await res.json()) as QuestionSetResponse;
  return mapQuestionSetResponseToQuestionSet(data);
}

function mapQuestionSetResponseToQuestionSet(
  response: QuestionSetResponse
): QuestionSet {
  return {
    id: response.id,
    name: response.name,
    description: response.description,
    latestVersion: response.latest_version,
    createdDate: parsePreciseISO(response.created_date),
  };
}

interface QuestionSetResponse {
  id: string;
  name: string;
  description: string;
  latest_version: number;
  created_date: string;
}
