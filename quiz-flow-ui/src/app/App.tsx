import { Provider } from "react-redux";
import { store } from "src/redux/store/store";
import { ToastProvider } from "src/components/ToastNotification/ToastContext";
import { AppRouter } from "./AppRouter";

const App = () => {
  return (
    <Provider store={store}>
      <ToastProvider>
        <AppRouter />
      </ToastProvider>
    </Provider>
  );
};

export default App;
