import * as styles from "./styles.module.scss";
import { IoMenuOutline } from "react-icons/io5";

export const Navbar = (props: { triggerSidebar: () => void }) => {
  return (
    <nav className={styles.dashboardNavbar}>
      <button
        className={styles.toggleBtn}
        type="button"
        onClick={props.triggerSidebar}
      >
        <IoMenuOutline className={styles.icon} />
      </button>
    </nav>
  );
};
