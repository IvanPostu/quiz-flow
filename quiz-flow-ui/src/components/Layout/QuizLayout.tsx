import { Outlet, useNavigate } from "react-router-dom";
import * as styles from "./styles.module.scss";
import { Navbar } from "src/components/Navbar/Navbar";
import { IoHomeOutline } from "react-icons/io5";

export const QuizLayout = () => {
  const navigate = useNavigate();

  return (
    <div className={`${styles.layoutRoot} ${styles.quizLayout}`}>
      <div className={styles.main}>
        <Navbar
          mainIcon={<IoHomeOutline />}
          triggerSidebar={() => {
            if (confirm("Do you really want to leave the quiz unfinished?")) {
              navigate("/");
            }
          }}
        />
        <main className={styles.content}>
          <Outlet />
        </main>
      </div>
    </div>
  );
};
