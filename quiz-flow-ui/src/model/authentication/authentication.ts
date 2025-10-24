import { API_BASE_URL } from "src/constants/constants";
import { parsePreciseISO } from "../utils/parsePreciseISO";
import { ApiClientError } from "../utils/ApiClientError";

export async function signIn(
  username: string,
  password: string
): Promise<SignInResult> {
  const res = await fetch(API_BASE_URL + "/api/authentications/sign-in", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      username: username,
      password: password,
    }),
  });

  if (!res.ok) {
    let errorMessage: string = "";
    try {
      const json = await res.json();
      errorMessage = JSON.stringify(json);
      throw ApiClientError.fromJson(json);
    } catch (err) {
      if (err instanceof ApiClientError) {
        throw err;
      }
      throw new Error(
        `HTTP ${res.status}: ${errorMessage || (await res.text())}`
      );
    }
  }

  const data: SignInResponse = (await res.json()) as SignInResponse;

  return {
    accessTokenId: data.access_token_id,
    refreshTokenId: data.refresh_token_id,
    accessToken: data.access_token,
    authorizationScopes: data.authorization_scopes,
    accessTokenExpirationDate: parsePreciseISO(
      data.access_token_expiration_date
    ),
    refreshTokenExpirationDate: parsePreciseISO(
      data.refresh_token_expiration_date
    ),
  };
}

export interface SignInResult {
  accessTokenId: string;
  refreshTokenId: string;
  accessToken: string;
  authorizationScopes: string[];
  accessTokenExpirationDate: Date;
  refreshTokenExpirationDate: Date;
}

interface SignInResponse {
  access_token_id: string;
  refresh_token_id: string;
  access_token: string;
  authorization_scopes: string[];
  access_token_expiration_date: string;
  refresh_token_expiration_date: string;
}
