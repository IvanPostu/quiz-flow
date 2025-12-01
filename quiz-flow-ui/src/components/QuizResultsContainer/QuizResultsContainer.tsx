import { Fragment } from "react/jsx-runtime";
import * as styles from "./QuizResultsContainer.module.scss";
import { IoCheckmarkCircleOutline, IoHourglassOutline } from "react-icons/io5";
import { ListOfItems } from "../ListOfItems/ListOfItems";
import { LoaderDots } from "../LoaderDots/LoaderDots";
import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import * as quizresults from "src/model/quizresults/quizresults";
import { QuizResult } from "src/model/quizresults/QuizResult";
import { useIsMounted } from "src/hooks/useIsMounted";
import { useAppSelector } from "src/redux";
import { selectAccessToken } from "src/redux/authentication/authenticationSlice";
import globals from "src/styles/globalVariables";

const DEFAULT_LIMIT_OF_QUESTION_RESULTS = 10;

interface QuizResultsContainerStateType {
  quizResults: QuizResult[] | null;
  offset: number;
}

export const QuizResultsContainer = () => {
  const navigate = useNavigate();
  const accessToken = useAppSelector(selectAccessToken) || "";
  const isMounted = useIsMounted();
  const [state, setState] = useState<QuizResultsContainerStateType>({
    quizResults: null,
    offset: 0,
  });

  const internalFetchQuizResults = (offset: number) => {
    fetchQuestionResults(
      accessToken,
      DEFAULT_LIMIT_OF_QUESTION_RESULTS,
      offset,
      (result) => {
        if (!isMounted()) {
          return;
        }
        if (result === "error") {
          return;
        }
        setState((prevState) => ({
          ...prevState,
          quizResults: result,
          offset: offset,
        }));
      }
    );
  };

  useEffect(() => {
    internalFetchQuizResults(state.offset);
  }, []);

  const { quizResults } = state;
  return (
    <Fragment>
      <div className={styles.rootContainer}>
        <h2 className={styles.title}>Quiz results</h2>

        <div className={styles.main}>
          {quizResults === null ? (
            <LoaderDots />
          ) : (
            <ListOfItems
              items={quizResults.map((value) => {
                const isFinalized = Boolean(value.quizFinalizedDate);
                const status: "progress" | "done" = isFinalized
                  ? "done"
                  : "progress";
                const leftIcon = isFinalized ? (
                  <IoCheckmarkCircleOutline
                    color={globals.green400}
                    size={"30px"}
                  />
                ) : (
                  <IoHourglassOutline color={globals.yellow400} size={"30px"} />
                );
                const descriptionItems: string[] = [
                  `Status: ${status}`,
                  `Version: ${value.questionSetVersion}`,
                ];
                if (isFinalized) {
                  descriptionItems.push(
                    `Correct answers: ${value.correctAnswersCount!!}/${
                      value.questionCount
                    }`
                  );
                  descriptionItems.push(
                    `Score: ${
                      (value.correctAnswersCount!! / value.questionCount) * 100
                    }%`
                  );
                }

                return {
                  id: value.quizId,
                  title: value.questionSetName,
                  descriptionItems: descriptionItems,
                  left: <span>{leftIcon}</span>,
                };
              })}
              onItemClick={(id) => navigate(`/quiz/${id}`)}
            />
          )}
        </div>

        <div className={styles.bottomLink}>
          <Link to="#">Manage question sets</Link>
        </div>
      </div>
    </Fragment>
  );
};

async function fetchQuestionResults(
  accessToken: string,
  limit: number,
  offset: number,
  onComplete: (questionSets: QuizResult[] | "error") => void
) {
  try {
    const result: QuizResult[] = await quizresults.getQuizResultList(
      accessToken,
      limit,
      offset,
      "DESC"
    );
    onComplete(result);
  } catch (e) {
    console.error(e);
    onComplete("error");
  }
}
