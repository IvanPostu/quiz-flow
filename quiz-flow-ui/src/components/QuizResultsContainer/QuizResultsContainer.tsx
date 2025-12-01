import { Fragment } from "react/jsx-runtime";
import * as styles from "./QuizResultsContainer.module.scss";
import { IoEllipsisHorizontalOutline } from "react-icons/io5";
import { ListOfItems } from "../ListOfItems/ListOfItems";
import { LoaderDots } from "../LoaderDots/LoaderDots";
import { useState } from "react";
import { Link } from "react-router-dom";

const items = [
  {
    id: "1",
    title: "New Project Update",
    descriptionItems: [
      "Owner: Admin",
      "Lorem ipsum dolor sit amet consectetur adipisicing elit. Repellendus optio alias laudantium nostrum dolorem quas sint harum nisi, accusantium veniam enim itaque. Quaerat omnis sequi dolorum eos necessitatibus iusto earum?",
    ],
    right: <span>13 May</span>,
    left: (
      <span>
        <IoEllipsisHorizontalOutline />
      </span>
    ),
  },
  {
    id: "2",
    title: "Meeting Notes",
    descriptionItems: [
      "Owner: You",
      "Lorem ipsum, dolor sit amet consectetur adipisicing elit. Nemo incidunt, vero accusamus neque optio, hic voluptatem voluptate ex reiciendis tempore facere repudiandae.",
    ],
    right: <span>test</span>,
    left: (
      <span>
        <IoEllipsisHorizontalOutline />
      </span>
    ),
  },
];

export const QuizResultsContainer = () => {
  const [state, setState] = useState({
    isLoading: false,
    // items: [],
    items: items,
  });

  const { isLoading } = state;
  return (
    <Fragment>
      <div className={styles.rootContainer}>
        <h2 className={styles.title}>Quiz results</h2>

        <div className={styles.main}>
          {isLoading ? (
            <LoaderDots />
          ) : (
            <ListOfItems
              items={state.items}
              onItemClick={(id) => console.log(id)}
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
