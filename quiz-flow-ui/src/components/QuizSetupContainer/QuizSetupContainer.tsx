import { useLocation } from "react-router-dom";
import { Container } from "../Container/Container";
import * as styles from "./styles.module.scss";
import { CardContainer } from "../CardContainer/CardContainer";
import { FC, Fragment, useEffect, useState } from "react";
import * as questionsets from "src/model/questionsets/questionsets";
import * as questions from "src/model/questions/questions";
import { QuestionSet } from "src/model/questionsets/QuestionSet";
import { useAppSelector } from "src/redux";
import { selectAccessToken } from "src/redux/authentication/authenticationSlice";
import { useIsMounted } from "src/hooks/useIsMounted";
import { LoaderSpinner } from "../LoaderSpinner/LoaderSpinner";
import {
  Question,
  QuestionSetVersion,
} from "src/model/questions/QuestionSetVersion";
import { ListOfItems } from "../ListOfItems/ListOfItems";

export const QuizSetupContainer = () => {
  const [state, setState] = useState<{
    questionSet: QuestionSet | null;
    questionSetVersion: QuestionSetVersion | null;
  }>({
    questionSet: null,
    questionSetVersion: null,
  });
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const accessToken = useAppSelector(selectAccessToken) || "";
  const isMounted = useIsMounted();

  const questionSetId = searchParams.get("questionSetId") || "";
  useEffect(() => {
    fetchQuestionSet(accessToken, questionSetId, (questionSetResult) => {
      if (!isMounted()) {
        return;
      }
      if (questionSetResult === "error") {
        return;
      }

      setState((prevState) => ({
        ...prevState,
        questionSet: questionSetResult,
      }));
      fetchQuestionSetVersion(
        accessToken,
        questionSetId,
        questionSetResult.latestVersion,
        (questionSetVersionResult) => {
          if (!isMounted()) {
            return;
          }
          if (questionSetVersionResult === "error") {
            return;
          }
          setState((prevState) => ({
            ...prevState,
            questionSetVersion: questionSetVersionResult,
          }));
        }
      );
    });
  }, []);

  return (
    <Container>
      <CardContainer className={styles.rootContainer}>
        <div className={styles.titleContainer}>
          <h2>Quiz Setup</h2>
        </div>
        {state.questionSet === null ? (
          <LoaderSpinner />
        ) : (
          <KeyValueDisplay data={calculateData(state.questionSet)} />
        )}
        {state.questionSetVersion === null ? null : (
          <Fragment>
            <div className={styles.setupSection}>
              <button className={styles.btn}>Chose questions</button>
            </div>
            <ListOfQuestions
              questions={state.questionSetVersion.questions}
              onItemClick={() => {}}
            />
          </Fragment>
        )}
      </CardContainer>
    </Container>
  );
};

const ListOfQuestions: FC<{
  questions: Question[];
  onItemClick: (id: string) => void;
}> = (props) => {
  return (
    <div>
      <ListOfItems
        listContainerStyle={{ maxHeight: 500 }}
        items={props.questions.map((value, index) => ({
          id: value.id,
          title: value.question,
          descriptionItems: [],
          left: <span>{index}) </span>,
        }))}
        onItemClick={props.onItemClick}
      />
    </div>
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

function calculateData(signInResult: QuestionSet) {
  const result = Object.entries(signInResult || {}).reduce<
    Record<string, string>
  >((acc, [key, value]) => {
    acc[key] = "" + value;
    return acc;
  }, {});

  return result;
}

async function fetchQuestionSet(
  accessToken: string,
  questionSetId: string,
  onComplete: (questionSets: QuestionSet | "error") => void
) {
  try {
    const result = await questionsets.getQuestionSet(
      accessToken,
      questionSetId
    );
    onComplete(result);
  } catch (e) {
    console.error(e);
    onComplete("error");
  }
}

async function fetchQuestionSetVersion(
  accessToken: string,
  questionSetId: string,
  version: number,
  onComplete: (questionSetVersion: QuestionSetVersion | "error") => void
) {
  try {
    const result = await questions.getQuestionSetVersion(
      accessToken,
      questionSetId,
      version
    );
    onComplete(result);
  } catch (e) {
    console.error(e);
    onComplete("error");
  }
}
