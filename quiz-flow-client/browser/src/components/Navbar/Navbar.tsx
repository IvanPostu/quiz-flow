import * as styles from "./styles.module.scss";

export const Navbar = (props: { triggerSidebar: () => void }) => {
  return (
    <nav className={styles.dashboardNavbar}>
      <button
        className={styles.toggleBtn}
        type="button"
        onClick={props.triggerSidebar}
      >
        <i className="lni lni-grid-alt"></i>
      </button>
    </nav>
  );
};
