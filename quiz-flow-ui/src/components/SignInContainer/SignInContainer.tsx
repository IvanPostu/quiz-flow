import { useAppDispatch, useAppSelector } from "src/redux";
import { CardContainer } from "../CardContainer/CardContainer";
import { Container } from "../Container/Container";
import { LoaderSpinner } from "../LoaderSpinner/LoaderSpinner";
import * as styles from "./styles.module.scss";
import globalStyleVariables from "src/styles/globalVariables";
import { useEffect, useRef } from "react";
import {
  selectErrorMessage,
  selectIsAuthenticated,
  selectIsSignInRequestOngoing,
  signInAsync,
} from "src/redux/authentication/authenticationSlice";
import { useToast } from "../ToastNotification/ToastContext";
import { useNavigate } from "react-router-dom";
import { API_BASE_URL } from "src/constants/constants";

async function fetchAccessToken() {
  try {
    const res = await fetch(
      API_BASE_URL + "/api/authentications/access-token",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({}),
      }
    );
    const result = await res.json();
    console.log(result);
  } catch (e) {
    console.error(e);
  }
}

export const SignInContainer = () => {
  const usernameRef = useRef<HTMLInputElement>(null);
  const passwordRef = useRef<HTMLInputElement>(null);

  const dispatch = useAppDispatch();
  const isSignInOngoing = useAppSelector(selectIsSignInRequestOngoing);
  const errorMessage = useAppSelector(selectErrorMessage);
  const isAuthenticated = useAppSelector(selectIsAuthenticated);

  const { addToast } = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    if (isAuthenticated) {
      addToast("Authentication was successful", "success");
      navigate("/");
    }
  }, [isAuthenticated]);

  function submitHandler() {
    if (usernameRef.current && passwordRef.current) {
      dispatch(
        signInAsync({
          username: usernameRef.current.value,
          password: passwordRef.current.value,
        })
      );
    }
  }

  const isLoading = isSignInOngoing;

  useEffect(() => {
    fetchAccessToken();
  }, []);

  return (
    <Container>
      <CardContainer className={styles.rootContainer}>
        {isLoading && (
          <div className={styles.overlay}>
            <LoaderSpinner />
          </div>
        )}
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
          <button onClick={submitHandler}>Sign In</button>
        </div>
      </CardContainer>
    </Container>
  );
};
