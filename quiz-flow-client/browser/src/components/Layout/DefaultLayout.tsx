import { Outlet } from "react-router-dom";
import * as styles from "./styles.module.scss";

export const DefaultLayout = () => {
  return (
    <>
      <main>
        <Outlet />
      </main>
      <footer className={styles.footerText}>footer</footer>
    </>
  );
};
