import { API_BASE_URL } from "src/constants/constants";
import { parsePreciseISO } from "../utils/parsePreciseISO";
import { handleAndThrowIfNeeded } from "../utils/requestErrorHandler";
import { SignInResult } from "./SignInResult";

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

  await handleAndThrowIfNeeded(res);
  const data: SignInResponse = (await res.json()) as SignInResponse;
  return mapSignInResponseToSignInResult(data);
}

export async function signOut(): Promise<void> {
  const res = await fetch(API_BASE_URL + "/api/authentications/sign-out", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
  });

  await handleAndThrowIfNeeded(res);
}

export async function createAccessToken(): Promise<SignInResult> {
  const res = await fetch(API_BASE_URL + "/api/authentications/access-token", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({}),
  });

  await handleAndThrowIfNeeded(res);
  const data: SignInResponse = (await res.json()) as SignInResponse;
  return mapSignInResponseToSignInResult(data);
}

function mapSignInResponseToSignInResult(
  signInResponse: SignInResponse
): SignInResult {
  return {
    accessTokenId: signInResponse.access_token_id,
    refreshTokenId: signInResponse.refresh_token_id,
    accessToken: signInResponse.access_token,
    authorizationScopes: signInResponse.authorization_scopes,
    accessTokenExpirationDate: parsePreciseISO(
      signInResponse.access_token_expiration_date
    ),
    refreshTokenExpirationDate: parsePreciseISO(
      signInResponse.refresh_token_expiration_date
    ),
  };
}

interface SignInResponse {
  access_token_id: string;
  refresh_token_id: string;
  access_token: string;
  authorization_scopes: string[];
  access_token_expiration_date: string;
  refresh_token_expiration_date: string;
}
