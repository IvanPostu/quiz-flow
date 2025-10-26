import { ApiClientError } from "./ApiClientError";

export async function handleAndThrowIfNeeded(response: Response) {
  if (!response.ok) {
    let errorMessage: string = "";
    try {
      const json = await response.json();
      errorMessage = JSON.stringify(json);
      throw ApiClientError.fromJson(json);
    } catch (err) {
      if (err instanceof ApiClientError) {
        throw err;
      }
      throw new Error(
        `HTTP ${response.status}: ${errorMessage || (await response.text())}`
      );
    }
  }
}
