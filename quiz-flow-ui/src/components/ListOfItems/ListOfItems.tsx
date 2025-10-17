import * as styles from "./styles.module.scss";
import { IoSadOutline } from "react-icons/io5";

type ItemType = {
  id: string;
  title: string;
  descriptionItems: string[];
  left: React.ReactElement;
  right: React.ReactElement;
};

type ListOfItemsPropsType = {
  items: ItemType[];
  onItemClick: (id: string) => void;
};

export const ListOfItems = (props: ListOfItemsPropsType) => {
  return <InternalListOfItems {...props} />;
};

const InternalListOfItems = (props: ListOfItemsPropsType) => {
  const { items, onItemClick } = props;

  const children =
    items.length === 0 ? (
      <NoElementsWereFound />
    ) : (
      items.map((item, index) => (
        <div
          key={item.id}
          className={styles.itemRow}
          onClick={() => onItemClick && onItemClick(item.id)}
        >
          <div className={styles.left}>
            <span>{item.left}</span>
          </div>
          <div className={styles.middle}>
            <div className={styles.title}>{item.title}</div>
            {item.descriptionItems.map((value) => (
              <div key={value} className={styles.description}>
                {value}
              </div>
            ))}
          </div>
          <div className={styles.right}>{item.right}</div>
        </div>
      ))
    );

  return <div className={styles.list}>{children}</div>;
};

function NoElementsWereFound() {
  return (
    <div
      style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      <div>No elements were found</div>
      <IoSadOutline style={{ fontSize: 40, margin: "10px" }} />
    </div>
  );
}
