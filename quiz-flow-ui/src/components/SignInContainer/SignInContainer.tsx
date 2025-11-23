import { useAppDispatch, useAppSelector } from "src/redux";
import { CardContainer } from "../CardContainer/CardContainer";
import { Container } from "../Container/Container";
import { LoaderSpinner } from "../LoaderSpinner/LoaderSpinner";
import * as styles from "./styles.module.scss";
import globalStyleVariables from "src/styles/globalVariables";
import { Fragment, useEffect, useRef, useState } from "react";
import {
  mapSignInResultToSignInState,
  selectErrorMessage,
  selectIsAuthenticated,
  selectIsSignInRequestOngoing,
  setSignInResult,
  signInAsync,
} from "src/redux/authentication/authenticationSlice";
import { useToast } from "../ToastNotification/ToastContext";
import { useLocation, useNavigate } from "react-router-dom";
import { createAccessToken } from "src/model/authentications/authentications";
import { SignInResult } from "src/model/authentications/SignInResult";
import { BlurOverlay } from "../BlurOverlay/BlurOverlay";

interface SignInContainerState {
  isAccessTokenCreationOngoing: boolean;
  redirectTo: string;
  signInResult: SignInResult | null;
}

export const SignInContainer = () => {
  const dispatch = useAppDispatch();
  const isAuthenticated = useAppSelector(selectIsAuthenticated);
  const { addToast } = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  function submitHandler(username: string, password: string) {
    dispatch(
      signInAsync({
        username: username,
        password: password,
      })
    );
  }
  const [state, setState] = useState<SignInContainerState>(() => {
    const queryParams = new URLSearchParams(location.search);
    let redirectTo: string = "/";
    if (
      queryParams.get("redirectTo") !== null &&
      queryParams.get("redirectTo") !== "/sign-out"
    ) {
      redirectTo = queryParams.get("redirectTo")!!;
    }

    return {
      isAccessTokenCreationOngoing: true,
      redirectTo: redirectTo,
      signInResult: null,
    };
  });

  useEffect(() => {
    const accessTokenWasCreatedBasedOnRefreshToken =
      state.signInResult !== null;
    if (isAuthenticated) {
      if (!accessTokenWasCreatedBasedOnRefreshToken) {
        addToast("Authentication was successful", "success");
      }
      navigate(state.redirectTo);
    }
  }, [isAuthenticated, state.signInResult]);
  useEffect(() => {
    fetchAccessToken((signInResult) => {
      if (signInResult === "error") {
        setState((prevState) => ({
          ...prevState,
          isAccessTokenCreationOngoing: false,
        }));
      } else {
        setState((prevState) => ({
          ...prevState,
          isAccessTokenCreationOngoing: false,
          signInResult: signInResult,
        }));
      }
    });
  }, []);
  useEffect(() => {
    if (state.signInResult) {
      dispatch(
        setSignInResult(mapSignInResultToSignInState(state.signInResult))
      );
    }
  }, [state.signInResult]);

  return (
    <Container>
      <CardContainer className={styles.rootContainer}>
        {state.isAccessTokenCreationOngoing ? (
          <LoaderSpinner />
        ) : (
          <SignInCardContent submitHandler={submitHandler} />
        )}
      </CardContainer>
    </Container>
  );
};

async function fetchAccessToken(
  onComplete: (signInResult: SignInResult | "error") => void
) {
  try {
    const result = await createAccessToken();
    onComplete(result);
  } catch (e) {
    onComplete("error");
  }
}

function SignInCardContent(props: {
  submitHandler: (username: string, password: string) => void;
}) {
  const isSignInOngoing = useAppSelector(selectIsSignInRequestOngoing);
  const errorMessage = useAppSelector(selectErrorMessage);

  const usernameRef = useRef<HTMLInputElement>(null);
  const passwordRef = useRef<HTMLInputElement>(null);

  function internalSubmitHandler() {
    if (usernameRef.current && passwordRef.current) {
      props.submitHandler(usernameRef.current.value, passwordRef.current.value);
    }
  }

  return (
    <Fragment>
      {isSignInOngoing && <Loader />}
      <h2 className={styles.title}>Sign-In</h2>
      <div className={styles.element}>
        <label>
          <span>Username:</span>
          <input ref={usernameRef} type="text" required />
        </label>
      </div>
      <div className={styles.element}>
        <label>
          <span>Password:</span>
          <input ref={passwordRef} type="password" required />
        </label>
      </div>
      {errorMessage && (
        <div className={styles.element}>
          <span style={{ color: globalStyleVariables.red300 }}>
            {errorMessage}
          </span>
        </div>
      )}

      <div className={styles.element}>
        <button onClick={internalSubmitHandler}>Sign In</button>
      </div>
    </Fragment>
  );
}

function Loader() {
  return (
    <BlurOverlay>
      <LoaderSpinner />
    </BlurOverlay>
  );
}
