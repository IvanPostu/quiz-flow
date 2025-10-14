import { Fragment } from "react/jsx-runtime";
import * as styles from "./styles.module.scss";
import { useState } from "react";

interface SidebarItem {
  text: string;
  icon?: string;
}

type SidebarDropdown = {
  header: string | SidebarItem;
  items: Array<string>;
};

type SidebarElement = {
  headerText: string;
  items: Array<SidebarItem | SidebarDropdown>;
};

type SidebarPropsType = {
  sidebarIsShown: boolean;
  elements: SidebarElement[];
};

function isSidebarItem(obj: any): obj is SidebarItem {
  return obj && typeof obj.text === "string";
}

function isSidebarDropdown(obj: any): obj is SidebarDropdown {
  return obj && Boolean(obj.header);
}

export const Sidebar = (props: SidebarPropsType) => {
  const [state, setState] = useState({
    activeDropdownPaths: new Set<string>(),
  });

  return (
    <aside
      className={`${styles.sidebarRoot} ${
        props.sidebarIsShown ? "" : styles.sidebarCollapsed
      }`}
    >
      <div className={styles.sidebarContainer}>
        <div className={styles.sidebarLogo}>
          <a href="#">Quiz Flow</a>
        </div>
        <ul className={styles.sidebarNav}>
          {props.elements.map((element) => (
            <Fragment key={element.headerText}>
              <li className={styles.sidebarHeader}>{element.headerText}</li>
              {element.items.map((item) => {
                if (isSidebarItem(item)) {
                  return (
                    <li className={styles.sidebarItem} key={item.text}>
                      <a href="#" className={styles.sidebarLink}>
                        {item.icon && <i className={item.icon}></i>}
                        {item.text}
                      </a>
                    </li>
                  );
                }
                if (isSidebarDropdown(item)) {
                  const headerText =
                    typeof item.header === "string"
                      ? item.header
                      : item.header.text;
                  const dropdownPath = `${element.headerText}/${headerText}`;
                  const isActive = state.activeDropdownPaths.has(dropdownPath);
                  return (
                    <li className={styles.sidebarItem} key={headerText}>
                      <a
                        href="#"
                        className={`${styles.sidebarLink} ${
                          styles.hasDropdown
                        } ${isActive ? styles.active : ""}`}
                        onClick={() => {
                          setState((prevState) => {
                            const copiedSet = new Set([
                              ...prevState.activeDropdownPaths,
                            ]);
                            if (copiedSet.has(dropdownPath)) {
                              copiedSet.delete(dropdownPath);
                            } else {
                              copiedSet.add(dropdownPath);
                            }
                            return {
                              ...prevState,
                              activeDropdownPaths: copiedSet,
                            };
                          });
                        }}
                      >
                        {typeof item.header === "string" ? (
                          item.header
                        ) : (
                          <Fragment>
                            {item.header.icon && (
                              <i className={item.header.icon}></i>
                            )}
                            {item.header.text}
                          </Fragment>
                        )}
                      </a>
                      <ul className={styles.sidebarDropdown}>
                        <li className={styles.sidebarItem}>
                          {item.items.map((dropdownItem) => (
                            <a
                              href="#"
                              className={styles.sidebarLink}
                              key={dropdownItem}
                            >
                              {dropdownItem}
                            </a>
                          ))}
                        </li>
                      </ul>
                    </li>
                  );
                }
                console.error("Can't map item " + item);
                return <Fragment key={""} />;
              })}
            </Fragment>
          ))}
        </ul>
      </div>
    </aside>
  );
};
