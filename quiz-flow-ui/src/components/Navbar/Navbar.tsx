import { ReactElement } from "react";
import * as styles from "./styles.module.scss";
import { IoMenuOutline } from "react-icons/io5";

type NavbarPropsType = {
  triggerSidebar: () => void;
  mainIcon?: ReactElement;
};

export const Navbar = (props: NavbarPropsType) => {
  const mainIcon = props.mainIcon || <IoMenuOutline />;

  return (
    <nav className={styles.dashboardNavbar}>
      <button
        className={styles.toggleBtn}
        type="button"
        onClick={props.triggerSidebar}
      >
        <span className={styles.icon}>{mainIcon}</span>
      </button>
    </nav>
  );
};
