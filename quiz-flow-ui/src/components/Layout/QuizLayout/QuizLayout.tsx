import { Outlet } from "react-router-dom";
import * as styles from "./styles.module.scss";

export const QuizLayout = () => {
  return (
    <>
      <Outlet />
      {/* <footer className={styles.footerText}>footer</footer> */}
    </>
  );
};
