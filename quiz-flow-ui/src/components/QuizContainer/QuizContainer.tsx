import { FC, useCallback, useEffect, useState } from "react";
import * as styles from "./styles.module.scss";
import { Container } from "../Container/Container";
import { CardContainer } from "../CardContainer/CardContainer";
import { useParams } from "react-router-dom";
import * as quizzes from "src/model/quizzes/quizzes";
import * as quizresults from "src/model/quizresults/quizresults";
import { Quiz, QuizQuestion } from "src/model/quizzes/Quiz";
import { useAppSelector } from "src/redux";
import { selectAccessToken } from "src/redux/authentication/authenticationSlice";
import { useIsMounted } from "src/hooks/useIsMounted";
import { LoaderSpinner } from "../LoaderSpinner/LoaderSpinner";
import { BlurOverlay } from "../BlurOverlay/BlurOverlay";
import globals from "src/styles/globalVariables";
import { areSetsEqual } from "src/model/utils/areSetsEqual";
import { KeyValueContainer } from "../KeyValueContainer/KeyValueContainer";
import { QuizResult } from "src/model/quizresults/QuizResult";

const RED_BORDER = `2px solid ${globals.red300}`;
const GREEN_BORDER = `2px solid ${globals.green300}`;

type QuizContainerStateType = {
  currentQuizItemIndex: number;
  quizItems: Array<QuizItemType> | null;
  quizResult: QuizResult | null;
  isQuizSubmitOngoing: boolean;
  isFinalized: boolean;
};

const INITIAL_STATE: QuizContainerStateType = {
  currentQuizItemIndex: 0,
  quizItems: null,
  quizResult: null,
  isQuizSubmitOngoing: false,
  isFinalized: false,
};

interface QuizItemType {
  questionId: string;
  question: string;
  answerOptions: Array<string>;
  selectedAnswerIndexes: Set<number>;
  correctAnswerIndexes: Set<number>;
  correctAnswerExplanation: string;
}

export const QuizContainer = () => {
  const { quizId } = useParams();
  const accessToken = useAppSelector(selectAccessToken) || "";
  const isMounted = useIsMounted();
  const [state, setState] = useState<QuizContainerStateType>(
    () => INITIAL_STATE
  );

  function handleQuizResultIfFinalized(quizResult: Quiz) {
    if (!quizResult.finalizedDate) {
      return;
    }
    const questionsById = quizResult.questions.reduce((acc, value) => {
      acc[value.questionId] = value;
      return acc;
    }, {} as Record<string, QuizQuestion>);

    const quizItems: QuizItemType[] = quizResult.answers.map((value) => {
      const question: QuizQuestion = questionsById[value.questionId];
      return {
        question: question.question,
        questionId: value.questionId,
        selectedAnswerIndexes: new Set([...value.chosenAnswerIndexes]),
        answerOptions: question.answerOptions,
        correctAnswerIndexes: new Set([...question.correctAnswerIndexes]),
        correctAnswerExplanation: question.correctAnswerExplanation,
      };
    });
    setState((prevState) => ({
      ...prevState,
      quizItems: quizItems,
      currentQuizItemIndex: 0,
      isFinalized: true,
      finalizedAnswers: [],
    }));
  }

  const goToTheQuizItemByIndex = useCallback(
    (index: number) => {
      setState((prevState) => ({
        ...prevState,
        currentQuizItemIndex: index,
      }));
    },
    [state]
  );
  const finalizeCurrentQuestion = () => {
    if (!state.quizItems) {
      return;
    }
    const quizItemsLength = state.quizItems.length;
    const nextItemIndex = state.currentQuizItemIndex + 1;
    if (nextItemIndex < quizItemsLength) {
      goToTheQuizItemByIndex(nextItemIndex);
    } else {
      if (confirm("Do you want to submit the current quiz?")) {
        submitQuiz();
      }
    }
  };

  const onEnterPress = useCallback((e: KeyboardEvent) => {
    if (e.key === "Enter") {
      finalizeCurrentQuestion();
    }
  }, []);

  useEffect(() => {
    const callback = onEnterPress;
    window.addEventListener("keydown", callback);
    return () => {
      window.removeEventListener("keydown", callback);
    };
  }, []);
  useEffect(() => {
    if (state.isFinalized) {
      fetchQuizResult(accessToken, quizId || "", (result) => {
        if (!isMounted()) {
          return;
        }
        if (result === "error") {
          return;
        }
        setState((prevState) => ({
          ...prevState,
          quizResult: result,
        }));
      });
    }
  }, [state.isFinalized]);
  useEffect(() => {
    fetchQuiz(accessToken, quizId || "", (quizResult) => {
      if (!isMounted()) {
        return;
      }
      if (quizResult === "error") {
        return;
      }
      const quizItems: QuizItemType[] = quizResult.questions.map((value) => {
        return {
          question: value.question,
          questionId: value.questionId,
          answerOptions: value.answerOptions,
          selectedAnswerIndexes: new Set(),
          correctAnswerIndexes: new Set(),
          correctAnswerExplanation: "",
        };
      });

      setState((prevState) => ({
        ...prevState,
        quizItems: quizItems,
        currentQuizItemIndex: 0,
      }));
      handleQuizResultIfFinalized(quizResult);
    });
  }, []);

  const selectAnswer = useCallback(
    (answerIndex: number) => {
      setState((prevState) => {
        if (!prevState.quizItems || state.isFinalized) {
          return prevState;
        }
        const quizItemIndex = prevState.currentQuizItemIndex;
        const currentQuizItem = prevState.quizItems[quizItemIndex];
        const newSet = new Set(currentQuizItem.selectedAnswerIndexes);
        if (newSet.has(answerIndex)) {
          newSet.delete(answerIndex);
        } else {
          newSet.add(answerIndex);
        }
        currentQuizItem.selectedAnswerIndexes = newSet;

        return {
          ...prevState,
        };
      });
    },
    [state]
  );

  if (state.quizItems === null) {
    return (
      <Container>
        <CardContainer className={styles.quizRoot}>
          <LoaderSpinner />
        </CardContainer>
      </Container>
    );
  }

  function submitQuiz() {
    const answers: quizzes.QuizAnswerRequest[] = (state.quizItems || []).map(
      (value) => {
        return {
          question_id: value.questionId,
          chosen_answer_indexes: Array.from(value.selectedAnswerIndexes),
        };
      }
    );
    setState((prevState) => ({
      ...prevState,
      isQuizSubmitOngoing: true,
    }));
    submitQuizToTheServer(
      accessToken,
      quizId || "",
      {
        finalize: true,
        answers: answers,
      },
      (quizResult) => {
        if (!isMounted()) {
          return;
        }
        setState((prevState) => ({
          ...prevState,
          isQuizSubmitOngoing: false,
        }));
        if (quizResult === "error") {
          return;
        }
        handleQuizResultIfFinalized(quizResult);
      }
    );
  }

  const quizItem: QuizItemType = state.quizItems[state.currentQuizItemIndex];
  return (
    <Container>
      <CardContainer className={styles.quizRoot}>
        {state.isQuizSubmitOngoing && <SubmitQuizLoader />}
        <div className={styles.quizHeader}>
          {state.quizResult && (
            <SummaryResult
              correctAnswersCount={state.quizResult.correctAnswersCount}
              questionsCount={state.quizResult.questionCount}
            />
          )}

          <div className={styles.questionsNavItems}>
            {state.quizItems.map((item, index) => {
              let className = `${styles.questionNavItem} `;
              if (index === state.currentQuizItemIndex) {
                className += " " + styles.active;
              }
              if (item.selectedAnswerIndexes.size > 0) {
                className += " " + styles.answered;
              }

              let borderStyle = "";
              if (state.isFinalized) {
                const isRight = areSetsEqual(
                  item.selectedAnswerIndexes,
                  item.correctAnswerIndexes
                );
                if (isRight) {
                  borderStyle = GREEN_BORDER;
                } else {
                  borderStyle = RED_BORDER;
                }
              }

              return (
                <div
                  style={{ border: borderStyle }}
                  className={className}
                  onClick={() => goToTheQuizItemByIndex(index)}
                  key={item.questionId}
                ></div>
              );
            })}
          </div>
          <div className={styles.quizQuestionContainer}>
            <span className={styles.quizQuestion}>{quizItem.question}</span>
          </div>
        </div>
        <div className={styles.quizBody}>
          <ul>
            {quizItem.answerOptions.map((value, index) => {
              const isChecked = quizItem.selectedAnswerIndexes.has(index);

              let borderStyle = "";
              if (state.isFinalized) {
                const isRight = quizItem.correctAnswerIndexes.has(index);
                if (!isRight && isChecked) {
                  borderStyle = RED_BORDER;
                } else if (isRight) {
                  borderStyle = GREEN_BORDER;
                }
              }

              return (
                <li key={value}>
                  <label
                    style={{ border: borderStyle }}
                    className={styles.answerOptionLabel}
                  >
                    <input
                      type="checkbox"
                      checked={isChecked}
                      onChange={() => selectAnswer(index)}
                    />
                    <span>{value}</span>
                  </label>
                </li>
              );
            })}
          </ul>
          {quizItem.correctAnswerExplanation && (
            <div className={styles.correctAnswerExplanationContainer}>
              <span>{quizItem.correctAnswerExplanation}</span>
            </div>
          )}
        </div>
        <button disabled={state.isFinalized} onClick={finalizeCurrentQuestion}>
          Submit
        </button>
      </CardContainer>
    </Container>
  );
};

const SummaryResult: FC<{
  questionsCount: number;
  correctAnswersCount: number;
}> = (props) => {
  const score = Math.floor(
    (props.correctAnswersCount / props.questionsCount) * 100
  );

  return (
    <div className={styles.summaryResult}>
      <KeyValueContainer
        data={{
          Score: `${score}%`,
          Questions: "" + props.questionsCount,
          "Correct answers": "" + props.correctAnswersCount,
        }}
      />
    </div>
  );
};

function SubmitQuizLoader() {
  return (
    <BlurOverlay>
      <LoaderSpinner />
    </BlurOverlay>
  );
}

async function fetchQuiz(
  accessToken: string,
  quizId: string,
  onComplete: (quiz: Quiz | "error") => void
) {
  try {
    const result = await quizzes.getQuiz(accessToken, quizId);
    onComplete(result);
  } catch (e) {
    console.error(e);
    onComplete("error");
  }
}

async function submitQuizToTheServer(
  accessToken: string,
  quizId: string,
  quizUpdateRequest: quizzes.QuizUpdateRequest,
  onComplete: (quiz: Quiz | "error") => void
) {
  try {
    const result = await quizzes.updateQuiz(
      accessToken,
      quizId,
      quizUpdateRequest
    );
    onComplete(result);
  } catch (e) {
    console.error(e);
    onComplete("error");
  }
}

async function fetchQuizResult(
  accessToken: string,
  quizId: string,
  onComplete: (quiz: QuizResult | "error") => void
) {
  try {
    const result = await quizresults.getQuizResult(accessToken, quizId);
    onComplete(result);
  } catch (e) {
    console.error(e);
    onComplete("error");
  }
}
