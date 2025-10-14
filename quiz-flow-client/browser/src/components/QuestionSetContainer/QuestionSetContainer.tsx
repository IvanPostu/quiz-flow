import * as styles from "./styles.module.scss";

export const QuestionSetContainer = () => {
  return <ItemList />;
};

const items = [
  {
    id: 1,
    title: "First Item",
    description: "This is the description for the first item.",
    link: "#",
  },
  {
    id: 2,
    title: "Second Item",
    description: "Details about the second item go here.",
    link: "#",
  },
  {
    id: 3,
    title: "Third Item",
    description: "Some more information about the third item.",
    link: "#",
  },
];

const ItemList = () => {
  return (
    <div className={styles.rootContainer}>
      <h2 className={styles.title}>Question Set Items</h2>

      {items.map((item) => (
        <div key={item.id} className={styles.card}>
          <h3>{item.title}</h3>
          <p>{item.description}</p>
          <a href={item.link} target="_blank" rel="noopener noreferrer">
            Take a quiz
          </a>
        </div>
      ))}

      <div className={styles.bottomLink}>
        <a href="#">See more...</a>
      </div>
    </div>
  );
};
