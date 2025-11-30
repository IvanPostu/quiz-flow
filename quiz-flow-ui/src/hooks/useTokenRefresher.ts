import { useEffect } from "react";
import { useAppDispatch, useAppSelector } from "src/redux";
import {
  SignInStateType,
  fetchNewAccessToken,
  selectIsSignInRequestOngoing,
} from "src/redux/authentication/authenticationSlice";
import { parsePreciseISO } from "src/model/utils/parsePreciseISO";

export function useTokenRefresher() {
  const signInResult: SignInStateType | null = useAppSelector(
    (state) => state.authentication.signInResult
  );
  const isSignInRequestOngoing = useAppSelector(selectIsSignInRequestOngoing);
  const dispatch = useAppDispatch();

  useEffect(() => {
    if (!signInResult) return;

    const now = new Date();
    const expirationDate = parsePreciseISO(
      signInResult.accessTokenExpirationIsoDate
    );
    const expiresInMs = expirationDate.getTime() - now.getTime();
    let refreshDelay = expiresInMs - 30_000;

    if (refreshDelay <= 0) {
      if (isSignInRequestOngoing) {
        return;
      }
      dispatch(fetchNewAccessToken());
      return;
    }

    const id: ReturnType<typeof setTimeout> = setTimeout(refresh, refreshDelay);

    function refresh() {
      dispatch(fetchNewAccessToken());
    }

    return () => clearTimeout(id);
  }, [signInResult]);
}
