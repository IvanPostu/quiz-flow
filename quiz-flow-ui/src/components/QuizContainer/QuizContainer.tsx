import { useCallback, useState } from "react";
import * as styles from "./styles.module.scss";
import { Container } from "../Container/Container";
import { CardContainer } from "../CardContainer/CardContainer";

const QUIZZES = [
  {
    question:
      "1 Lorem ipsum dolor sit amet consectetur, adipisicing elit. Esse dolore sed magni vero, neque vitae repudiandae laboriosam qui ullam laborum iure atque quasi consequatur debitis modi! Quae nam ipsa magni eaque eius maiores laudantium mollitia ad delectus qui. Odit neque labore reprehenderit voluptatum ullam harum magni quisquam sequi fugit perferendis, odio quae aut at atque debitis dolorum reiciendis laboriosam delectus deserunt voluptate eligendi molestiae ipsa dolores totam. Maxime consequatur soluta esse ipsam illum alias enim fuga quae? Consectetur veritatis a delectus illum, modi ipsa necessitatibus. Expedita assumenda eos alias optio harum quasi recusandae accusamus quaerat modi laudantium iure, nobis fugiat! Aut veniam, assumenda nam eligendi reiciendis sed nesciunt laborum nobis tenetur. Non praesentium maxime dolore illum a et voluptate quasi amet nam eligendi id rem, ratione minima adipisci commodi eveniet facilis eaque doloremque ad nostrum. Cupiditate labore ducimus distinctio delectus quod tempora, debitis dolore totam? Nemo neque optio dicta rerum ad totam officiis dignissimos? Odio, nobis quibusdam! Aperiam nemo totam esse quisquam veniam labore officia iusto, ipsa ullam officiis quia ducimus commodi maxime sunt veritatis in natus tempora rem, inventore impedit facere? Fugit id distinctio labore reprehenderit velit? Quidem a laboriosam rerum quasi ullam harum, commodi optio possimus veritatis sequi!",
    answers: [
      "1Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.",
      "2Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.",
      "3Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.",
      "4Lorem ipsum dolor sit amet consectetur adipisicing elit. Cupiditate dolorum nesciunt, mollitia adipisci molestias dolore officiis, officia ea aliquam provident, earum maiores.",
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

type QuizContainerStateType = {
  currentItemElement: number;
  quizzItems: Array<{
    question: string;
    answers: Array<string>;
    selectedAnswerIndexes: Set<number>;
  }>;
};

export const QuizContainer = () => {
  const [state, setState] = useState<QuizContainerStateType>(() => {
    return {
      currentItemElement: 0,
      quizzItems: QUIZZES.map((quiz) => ({
        question: quiz.question,
        answers: quiz.answers,
        selectedAnswerIndexes: new Set(),
      })),
    };
  });
  const goToTheNextQuizItem = useCallback(
    (nextItemElement: number) => {
      setState((prevState) => ({
        ...prevState,
        currentItemElement: nextItemElement,
      }));
    },
    [state]
  );
  const selectAnswer = useCallback(
    (answerIndex: number) => {
      setState((prevState) => {
        const quizItemIndex = prevState.currentItemElement;
        const currentQuizItem = prevState.quizzItems[quizItemIndex];
        currentQuizItem.selectedAnswerIndexes.add(answerIndex);
        return {
          ...prevState,
        };
      });
    },
    [state]
  );

  const quizItem = state.quizzItems[state.currentItemElement];

  return (
    <Container>
      <CardContainer className={styles.quizRoot}>
        <div className={styles.quizHeader}>
          <div className={styles.questionsNavItems}>
            {state.quizzItems.map((item, index) => {
              let className = "";
              if (index === state.currentItemElement) {
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
            {quizItem.answers.map((value, index) => (
              <li key={value}>
                <label>
                  <input
                    type="checkbox"
                    checked={quizItem.selectedAnswerIndexes.has(index)}
                    onChange={() => selectAnswer(index)}
                  />
                  {value}
                </label>
              </li>
            ))}
          </ul>
        </div>
        <button
          onClick={() => {
            const nextItemIndex = state.currentItemElement + 1;
            if (nextItemIndex < state.quizzItems.length) {
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
