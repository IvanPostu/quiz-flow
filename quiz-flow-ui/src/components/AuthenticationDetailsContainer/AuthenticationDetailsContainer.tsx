import { differenceInSeconds } from "date-fns";
import { useEffect, useState } from "react";
import { useAppSelector } from "src/redux";
import { SignInStateType } from "src/redux/authentication/authenticationSlice";
import { CardContainer } from "../CardContainer/CardContainer";
import { Container } from "../Container/Container";
import * as styles from "./style.module.scss";
import { KeyValueContainer } from "../KeyValueContainer/KeyValueContainer";

export const AuthenticationDetailsContainer = () => {
  const [seconds, setSeconds] = useState<number>(0);

  const signInResult = useAppSelector(
    (state) => state.authentication.signInResult
  );
  const data = calculateData(signInResult);

  useEffect(() => {
    const intervalId = setInterval(() => {
      setSeconds((prev) => prev + 1);
    }, 1000);
    return () => clearInterval(intervalId);
  }, []);

  return (
    <Container>
      <CardContainer className={styles.rootContainer}>
        <div className={styles.titleContainer}>
          <h2>Authentication Details</h2>
        </div>
        <KeyValueContainer data={data} />
      </CardContainer>
    </Container>
  );
};

function calculateData(signInResult: SignInStateType | null) {
  const result = Object.entries(signInResult || {}).reduce<
    Record<string, string>
  >((acc, [key, value]) => {
    if (key.endsWith("IsoDate")) {
      acc[key] = "" + prettifyIsoDate(value);
    } else {
      acc[key] = "" + value;
    }
    return acc;
  }, {});
  result["refreshable_token"] =
    '<Is located in cookie with the key: "refreshable_token">';

  return result;
}

function prettifyIsoDate(isoDate: string): string {
  const date = new Date(isoDate);
  const now = new Date();

  const diffInSeconds = differenceInSeconds(date, now);

  const hours = Math.floor(diffInSeconds / 3600); // 3600 seconds in an hour
  const minutes = Math.floor((diffInSeconds % 3600) / 60); // Remaining minutes
  const seconds = diffInSeconds % 60; // Remaining seconds

  return `${isoDate} - Expires in: ${hours} hours, ${minutes} minutes, ${seconds} seconds`;
}
