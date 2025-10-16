import * as styles from "./styles.module.scss";

export const LoaderSpinner = () => {
  return (
    <div className={styles.loaderContainer}>
      <div className={styles.spinner}></div>
    </div>
  );
};
