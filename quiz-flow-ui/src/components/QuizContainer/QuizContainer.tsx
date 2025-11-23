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

type QuizContainerStateType = {
  currentQuizItemIndex: number;
  quizItems: Array<QuizItemType> | null;
};

interface QuizItemType {
  questionId: string;
  question: string;
  answerOptions: Array<string>;
  selectedAnswerIndexes: Set<number>;
}

const QUIZZES = [
  {
    question:
      "test1\ntest2\ntest3\ntest4\ntest5\ntest6\ntest7\ntest8\n1 Lorem ipsum dolor sit amet consectetur, adipisicing elit. Esse dolore sed magni vero, neque vitae repudiandae laboriosam qui ullam laborum iure atque quasi consequatur debitis modi! Quae nam ipsa magni eaque eius maiores laudantium mollitia ad delectus qui. Odit neque labore reprehenderit voluptatum ullam harum magni quisquam sequi fugit perferendis, odio quae aut at atque debitis dolorum reiciendis laboriosam delectus deserunt voluptate eligendi molestiae ipsa dolores totam. Maxime consequatur soluta esse ipsam illum alias enim fuga quae? Consectetur veritatis a delectus illum, modi ipsa necessitatibus. Expedita assumenda eos alias optio harum quasi recusandae accusamus quaerat modi laudantium iure, nobis fugiat! Aut veniam, assumenda nam eligendi reiciendis sed nesciunt laborum nobis tenetur. Non praesentium maxime dolore illum a et voluptate quasi amet nam eligendi id rem, ratione minima adipisci commodi eveniet facilis eaque doloremque ad nostrum. Cupiditate labore ducimus distinctio delectus quod tempora, debitis dolore totam? Nemo neque optio dicta rerum ad totam officiis dignissimos? Odio, nobis quibusdam! Aperiam nemo totam esse quisquam veniam labore officia iusto, ipsa ullam officiis quia ducimus commodi maxime sunt veritatis in natus tempora rem, inventore impedit facere? Fugit id distinctio labore reprehenderit velit? Quidem a laboriosam rerum quasi ullam harum, commodi optio possimus veritatis sequi!",
    answers: [
      "1Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.1Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.1Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.1Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.1Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.1Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.",
      "2Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.",
      "test1\ntest2\ntest3\ntest4\ntest5\ntest6\ntest7\ntest8\n",
      "test 123",
    ],
  },
  {
    question:
      "2 Lorem ipsum dolor sit amet consectetur, adipisicing elit. Esse dolore sed magni vero, neque vitae repudiandae laboriosam qui ullam laborum iure atque quasi consequatur debitis modi! Quae nam ipsa magni eaque eius maiores laudantium mollitia ad delectus qui. Odit neque labore reprehenderit voluptatum ullam harum magni quisquam sequi fugit perferendis, odio quae aut at atque debitis dolorum reiciendis laboriosam delectus deserunt voluptate eligendi molestiae ipsa dolores totam. Maxime consequatur soluta esse ipsam illum alias enim fuga quae? Consectetur veritatis a delectus illum, modi ipsa necessitatibus. Expedita assumenda eos alias optio harum quasi recusandae accusamus quaerat modi laudantium iure, nobis fugiat! Aut veniam, assumenda nam eligendi reiciendis sed nesciunt laborum nobis tenetur. Non praesentium maxime dolore illum a et voluptate quasi amet nam eligendi id rem, ratione minima adipisci commodi eveniet facilis eaque doloremque ad nostrum. Cupiditate labore ducimus distinctio delectus quod tempora, debitis dolore totam? Nemo neque optio dicta rerum ad totam officiis dignissimos? Odio, nobis quibusdam! Aperiam nemo totam esse quisquam veniam labore officia iusto, ipsa ullam officiis quia ducimus commodi maxime sunt veritatis in natus tempora rem, inventore impedit facere? Fugit id distinctio labore reprehenderit velit? Quidem a laboriosam rerum quasi ullam harum, commodi optio possimus veritatis sequi!",
    answers: [
      "1Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.",
      "2Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.",
      "3Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.",
      "4Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.",
    ],
  },
  {
    question:
      "3 Lorem ipsum dolor sit amet consectetur, adipisicing elit. Esse dolore sed magni vero, neque vitae repudiandae laboriosam qui ullam laborum iure atque quasi consequatur debitis modi! Quae nam ipsa magni eaque eius maiores laudantium mollitia ad delectus qui. Odit neque labore reprehenderit voluptatum ullam harum magni quisquam sequi fugit perferendis, odio quae aut at atque debitis dolorum reiciendis laboriosam delectus deserunt voluptate eligendi molestiae ipsa dolores totam. Maxime consequatur soluta esse ipsam illum alias enim fuga quae? Consectetur veritatis a delectus illum, modi ipsa necessitatibus. Expedita assumenda eos alias optio harum quasi recusandae accusamus quaerat modi laudantium iure, nobis fugiat! Aut veniam, assumenda nam eligendi reiciendis sed nesciunt laborum nobis tenetur. Non praesentium maxime dolore illum a et voluptate quasi amet nam eligendi id rem, ratione minima adipisci commodi eveniet facilis eaque doloremque ad nostrum. Cupiditate labore ducimus distinctio delectus quod tempora, debitis dolore totam? Nemo neque optio dicta rerum ad totam officiis dignissimos? Odio, nobis quibusdam! Aperiam nemo totam esse quisquam veniam labore officia iusto, ipsa ullam officiis quia ducimus commodi maxime sunt veritatis in natus tempora rem, inventore impedit facere? Fugit id distinctio labore reprehenderit velit? Quidem a laboriosam rerum quasi ullam harum, commodi optio possimus veritatis sequi!",
    answers: [
      "1Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.",
      "2Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.",
      "3Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.",
      "4Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.",
    ],
  },
  {
    question:
      "4 Lorem ipsum dolor sit amet consectetur, adipisicing elit. Esse dolore sed magni vero, neque vitae repudiandae laboriosam qui ullam laborum iure atque quasi consequatur debitis modi! Quae nam ipsa magni eaque eius maiores laudantium mollitia ad delectus qui. Odit neque labore reprehenderit voluptatum ullam harum magni quisquam sequi fugit perferendis, odio quae aut at atque debitis dolorum reiciendis laboriosam delectus deserunt voluptate eligendi molestiae ipsa dolores totam. Maxime consequatur soluta esse ipsam illum alias enim fuga quae? Consectetur veritatis a delectus illum, modi ipsa necessitatibus. Expedita assumenda eos alias optio harum quasi recusandae accusamus quaerat modi laudantium iure, nobis fugiat! Aut veniam, assumenda nam eligendi reiciendis sed nesciunt laborum nobis tenetur. Non praesentium maxime dolore illum a et voluptate quasi amet nam eligendi id rem, ratione minima adipisci commodi eveniet facilis eaque doloremque ad nostrum. Cupiditate labore ducimus distinctio delectus quod tempora, debitis dolore totam? Nemo neque optio dicta rerum ad totam officiis dignissimos? Odio, nobis quibusdam! Aperiam nemo totam esse quisquam veniam labore officia iusto, ipsa ullam officiis quia ducimus commodi maxime sunt veritatis in natus tempora rem, inventore impedit facere? Fugit id distinctio labore reprehenderit velit? Quidem a laboriosam rerum quasi ullam harum, commodi optio possimus veritatis sequi!",
    answers: [
      "1Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.",
      "2Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.",
      "3Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.",
      "4Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.",
    ],
  },
];

export const QuizContainer = () => {
  const { quizId } = useParams();
  const accessToken = useAppSelector(selectAccessToken) || "";
  const isMounted = useIsMounted();
  const [state, setState] = useState<QuizContainerStateType>(() => {
    return {
      currentQuizItemIndex: 0,
      quizItems: QUIZZES.map((quiz) => ({
        questionId: quiz.question,
        question: quiz.question,
        answerOptions: quiz.answers,
        selectedAnswerIndexes: new Set(),
      })),
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
      console.log(quizItems);
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

  const quizItem = state.quizItems[state.currentQuizItemIndex];
  const quizItemsLength = state.quizItems.length;
  return (
    <Container>
      <CardContainer className={styles.quizRoot}>
        <div className={styles.quizHeader}>
          <div className={styles.questionsNavItems}>
            {state.quizItems.map((item, index) => {
              let className = "";
              if (index === state.currentQuizItemIndex) {
                className += " " + styles.active;
              }
              if (item.selectedAnswerIndexes.size > 0) {
                className += " " + styles.answered;
              }
              return (
                <span
                  className={className}
                  onClick={() => goToTheNextQuizItem(index)}
                  key={item.question}
                ></span>
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
              confirm("Do you want to submit the current quiz?");
            }
          }}
        >
          Submit
        </button>
      </CardContainer>
    </Container>
  );
};

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
