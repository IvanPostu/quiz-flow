export class ApiClientError {
  uniqueId: string;
  errorCode: string;
  message: string;
  data: Record<string, string>;

  constructor(
    uniqueId: string,
    errorCode: string,
    message: string,
    data: Record<string, string>
  ) {
    this.uniqueId = uniqueId;
    this.errorCode = errorCode;
    this.message = message;
    this.data = data;
  }

  static fromJson(json: any): ApiClientError {
    return new ApiClientError(
      json.unique_id,
      json.error_code,
      json.message,
      json.data
    );
  }
}
