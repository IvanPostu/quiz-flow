const path = require("path");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const webpack = require("webpack");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");

module.exports = (env, argv) => {
  const isProduction = argv.mode === "production";
  console.log(`isProduction=${isProduction}`);

  const pluginsArray = [
    new HtmlWebpackPlugin({
      template: path.join(__dirname, "public", "index.html"),
    }),
    new MiniCssExtractPlugin({
      filename: isProduction ? "[name]-[contenthash].css" : "[name].css",
    }),
  ];
  if (!isProduction) {
    pluginsArray.push(new webpack.SourceMapDevToolPlugin({}));
  }

  return {
    mode: isProduction ? "production" : "development",
    entry: path.resolve(__dirname, "src", "index.ts"),
    output: {
      path: path.resolve(__dirname, "dist/public"),
      filename: isProduction ? "[name]-[contenthash].js" : "[name].js",
      publicPath: isProduction ? "/public/" : "",
      clean: true,
    },
    resolve: {
      extensions: [".tsx", ".ts", ".js"],
      alias: {
        src: path.resolve(__dirname, "src"),
      },
    },
    module: {
      rules: [
        {
          test: /\.(ts|tsx)$/,
          exclude: /node_modules/,
          use: "ts-loader",
        },
        {
          test: /\.css$/,
          use: [
            isProduction ? MiniCssExtractPlugin.loader : "style-loader",
            "css-loader",
          ],
        },
        {
          test: /\.module\.scss$/,
          use: [
            isProduction ? MiniCssExtractPlugin.loader : "style-loader",
            {
              loader: "css-loader",
              options: {
                modules: {
                  localIdentName: isProduction
                    ? "[hash:base64:5]"
                    : "[name]__[local]___[hash:base64:5]",
                },
              },
            },
            "sass-loader",
          ],
        },
        {
          test: /\.scss$/,
          exclude: /\.module\.scss$/,
          use: [
            isProduction ? MiniCssExtractPlugin.loader : "style-loader",
            "css-loader",
            "sass-loader",
          ],
        },
      ],
    },
    plugins: pluginsArray,
    stats: {
      errorDetails: !isProduction,
    },
    devtool: false,
    devServer: {
      static: path.join(__dirname, "public"),
      compress: true,
      port: 3000,
      historyApiFallback: true,
      hot: true,
      open: false,
      proxy: [
        {
          context: ["/api"],
          target: "http://127.0.0.1:9000",
          secure: false,
          changeOrigin: true,
          headers: {
            "Access-Control-Allow-Origin": "*",
          },
        },
      ],
    },
  };
};
