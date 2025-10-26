import { useAppSelector } from "src/redux";
import { CardContainer } from "../CardContainer/CardContainer";
import { Container } from "../Container/Container";
import * as styles from "./style.module.scss";
import { useEffect, useMemo, useState } from "react";
import { differenceInMinutes, differenceInSeconds } from "date-fns";
import { SignInStateType } from "src/redux/authentication/authenticationSlice";

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
        <KeyValueDisplay data={data} />
      </CardContainer>
    </Container>
  );
};

interface KeyValueDisplayProps {
  data: Record<string, string>;
}

const KeyValueDisplay: React.FC<KeyValueDisplayProps> = ({ data }) => {
  return (
    <div className={styles.keyValueContainer}>
      {Object.entries(data).map(([key, value]) => (
        <div className={styles.keyValueRow} key={key}>
          <div className={styles.key}>{key}</div>
          <div className={styles.value}>{value}</div>
        </div>
      ))}
    </div>
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
