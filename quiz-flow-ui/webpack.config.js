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
        ? "assets/[name].[contenthash][ext][query]"
        : "[name].[ext]",
      publicPath: isProduction ? "/public/" : "/",
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
          test: /\.(s?css)$/,
          exclude: /\.module\.s?css$/,
          use: [
            isProduction ? MiniCssExtractPlugin.loader : "style-loader",
            "css-loader",
            "sass-loader",
          ],
        },
        {
          test: /\.module\.s?css$/,
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
      ],
    },
    plugins: pluginsArray,
    stats: isProduction ? "normal" : "errors-warnings",
    optimization: {
      minimize: true,
      minimizer: [
        new TerserPlugin(), // Minifies JS
        new CssMinimizerPlugin(), // Minifies CSS
      ],
      splitChunks: {
        chunks: "all",
        cacheGroups: {
          reactVendor: {
            test: /[\\/]node_modules[\\/](react|react-dom|react-router-dom)[\\/]/,
            name: "react-vendor",
            chunks: "all",
          },
          otherVendors: {
            test: /[\\/]node_modules[\\/]/,
            name: "vendors",
            chunks: "all",
            priority: -10,
          },
        },
      },
      runtimeChunk: {
        name: "runtime",
      },
    },
    performance: {
      hints: isProduction ? "warning" : false, // Or false to disable
    },
    devtool: isProduction ? false : "eval-source-map",
    devServer: {
      static: path.join(__dirname, "public"),
      compress: true,
      host: "0.0.0.0",
      port: 3000,
      historyApiFallback: true,
      hot: true,
      open: false,
      proxy: [
        {
          context: ["/api"],
          target: "http://127.0.0.1:8080",
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
