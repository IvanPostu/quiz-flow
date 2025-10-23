const path = require("path");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const webpack = require("webpack");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const CssMinimizerPlugin = require("css-minimizer-webpack-plugin");
const TerserPlugin = require("terser-webpack-plugin");
const { BundleAnalyzerPlugin } = require("webpack-bundle-analyzer");

module.exports = (env, argv) => {
  const isProduction = process.env.NODE_ENV === "production";
  const buildAnalyze = process.env.BUILD_ANALYZE === "true";
  console.log(`isProduction=${isProduction},buildAnalyze=${buildAnalyze}`);

  const pluginsArray = [
    new HtmlWebpackPlugin({
      template: path.join(__dirname, "public", "index.html"),
      minify: {
        collapseWhitespace: true,
        removeComments: true,
        removeRedundantAttributes: true,
        useShortDoctype: true,
      },
    }),
    new MiniCssExtractPlugin({
      filename: isProduction ? "[name].[contenthash].css" : "[name].css",
    }),
  ];
  if (!isProduction) {
    pluginsArray.push(new webpack.SourceMapDevToolPlugin({}));
  }
  if (buildAnalyze) {
    pluginsArray.push(
      new BundleAnalyzerPlugin({
        analyzerMode: "static",
        openAnalyzer: false,
        reportFilename: "report.html",
      })
    );
  }

  return {
    mode: isProduction ? "production" : "development",
    entry: path.resolve(__dirname, "src", "index.ts"),
    output: {
      path: path.resolve(__dirname, "dist", "public"),
      filename: isProduction ? "[name].[contenthash].js" : "[name].js",
      assetModuleFilename: isProduction
        ? "assets/[hash][ext][query]"
        : "[name].[ext]",
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
          test: /\.scss$/,
          exclude: /\.module\.scss$/,
          use: [
            isProduction ? MiniCssExtractPlugin.loader : "style-loader",
            "css-loader",
            "sass-loader",
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
          test: /\.(woff|woff2|eot|ttf|otf)$/i,
          type: "asset/resource",
        },
      ],
    },
    plugins: pluginsArray,
    stats: {
      errorDetails: !isProduction,
      modules: true,
      reasons: true,
      chunkModules: true,
    },
    optimization: {
      minimize: true,
      minimizer: [
        new TerserPlugin(), // Minifies JS
        new CssMinimizerPlugin(), // Minifies CSS
      ],
      splitChunks: {
        chunks: "all",
      },
      runtimeChunk: {
        name: "runtime",
      },
    },
    performance: {
      hints: isProduction ? "warning" : false, // Or false to disable
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
