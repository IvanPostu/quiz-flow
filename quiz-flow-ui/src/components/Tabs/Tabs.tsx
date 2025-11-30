import React, { useState } from "react";
import * as styles from "./Tabs.module.scss";

type TabsPropsType = {
  tabs: Array<{
    id: string;
    label: string;
    content: React.ReactNode;
  }>;
  onTabChanged?: (id: string) => void;
};

export function Tabs({ tabs, onTabChanged }: TabsPropsType) {
  const [active, setActive] = useState(tabs[0]?.id);

  const activeTab = tabs.find((t) => t.id === active);

  return (
    <div className={styles.wrapper}>
      <div className={styles.tabs}>
        {tabs.map((tab) => (
          <div
            key={tab.id}
            className={`${styles.tab} ${
              active === tab.id ? styles.active : ""
            }`}
            onClick={() => {
              setActive(tab.id);
              onTabChanged && onTabChanged(tab.id);
            }}
          >
            {tab.label}
          </div>
        ))}
      </div>

      <div className={styles.content}>{activeTab?.content}</div>
    </div>
  );
}
