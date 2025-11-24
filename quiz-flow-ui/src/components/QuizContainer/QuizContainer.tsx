import { useCallback, useEffect, useState } from "react";
import * as styles from "./styles.module.scss";
import { Container } from "../Container/Container";
import { CardContainer } from "../CardContainer/CardContainer";
import { useParams } from "react-router-dom";
import * as quizzes from "src/model/quizzes/quizzes";
import { Quiz, QuizQuestion } from "src/model/quizzes/Quiz";
import { useAppSelector } from "src/redux";
import { selectAccessToken } from "src/redux/authentication/authenticationSlice";
import { useIsMounted } from "src/hooks/useIsMounted";
import { LoaderSpinner } from "../LoaderSpinner/LoaderSpinner";
import { BlurOverlay } from "../BlurOverlay/BlurOverlay";
import globals from "src/styles/globalVariables";
import { areSetsEqual } from "src/model/utils/areSetsEqual";

const RED_BORDER = `2px solid ${globals.red300}`;
const GREEN_BORDER = `2px solid ${globals.green300}`;

type QuizContainerStateType = {
  currentQuizItemIndex: number;
  quizItems: Array<QuizItemType> | null;
  isQuizSubmitOngoing: boolean;
  isFinalized: boolean;
};

const INITIAL_STATE: QuizContainerStateType = {
  currentQuizItemIndex: 0,
  quizItems: null,
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

  const goToTheNextQuizItem = useCallback(
    (nextItemElement: number) => {
      setState((prevState) => ({
        ...prevState,
        currentQuizItemIndex: nextItemElement,
      }));
    },
    [state]
  );
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

  const quizItem = state.quizItems[state.currentQuizItemIndex];
  const quizItemsLength = state.quizItems.length;
  return (
    <Container>
      <CardContainer className={styles.quizRoot}>
        {state.isQuizSubmitOngoing && <SubmitQuizLoader />}
        <div className={styles.quizHeader}>
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
                  onClick={() => goToTheNextQuizItem(index)}
                  key={item.questionId}
                ></div>
              );
            })}
          </div>
          <span className={styles.quizQuestion}>{quizItem.question}</span>
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
        </div>
        <button
          disabled={state.isFinalized}
          onClick={() => {
            const nextItemIndex = state.currentQuizItemIndex + 1;
            if (nextItemIndex < quizItemsLength) {
              goToTheNextQuizItem(nextItemIndex);
            } else {
              if (confirm("Do you want to submit the current quiz?")) {
                submitQuiz();
              }
            }
          }}
        >
          Submit
        </button>
      </CardContainer>
    </Container>
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
