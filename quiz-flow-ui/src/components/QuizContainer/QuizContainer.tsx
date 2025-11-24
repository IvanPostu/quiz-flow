import { useCallback, useEffect, useState } from "react";
import * as styles from "./styles.module.scss";
import { Container } from "../Container/Container";
import { CardContainer } from "../CardContainer/CardContainer";
import { useParams } from "react-router-dom";
import * as quizzes from "src/model/quizzes/quizzes";
import { Quiz } from "src/model/quizzes/Quiz";
import { useAppSelector } from "src/redux";
import { selectAccessToken } from "src/redux/authentication/authenticationSlice";
import { useIsMounted } from "src/hooks/useIsMounted";
import { LoaderSpinner } from "../LoaderSpinner/LoaderSpinner";
import { BlurOverlay } from "../BlurOverlay/BlurOverlay";

type QuizContainerStateType = {
  currentQuizItemIndex: number;
  quizItems: Array<QuizItemType> | null;
  isQuizSubmitOngoing: boolean;
};

interface QuizItemType {
  questionId: string;
  question: string;
  answerOptions: Array<string>;
  selectedAnswerIndexes: Set<number>;
}

export const QuizContainer = () => {
  const { quizId } = useParams();
  const accessToken = useAppSelector(selectAccessToken) || "";
  const isMounted = useIsMounted();
  const [state, setState] = useState<QuizContainerStateType>(() => {
    return {
      currentQuizItemIndex: 0,
      quizItems: null,
      isQuizSubmitOngoing: false,
    };
  });

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
        };
      });

      setState((prevState) => ({
        ...prevState,
        quizItems: quizItems,
        currentQuizItemIndex: 0,
      }));
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
        if (!prevState.quizItems) {
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
        console.log(quizResult);
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
              return (
                <div
                  className={className}
                  onClick={() => goToTheNextQuizItem(index)}
                  key={item.question}
                ></div>
              );
            })}
          </div>
          <span className={styles.quizQuestion}>{quizItem.question}</span>
        </div>
        <div className={styles.quizBody}>
          <ul>
            {quizItem.answerOptions.map((value, index) => (
              <li key={value}>
                <label className={styles.answerOptionLabel}>
                  <input
                    type="checkbox"
                    checked={quizItem.selectedAnswerIndexes.has(index)}
                    onChange={() => selectAnswer(index)}
                  />
                  <span>{value}</span>
                </label>
              </li>
            ))}
          </ul>
        </div>
        <button
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
