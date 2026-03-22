const { merge } = require("webpack-merge");
const commonConfig = require("./webpack.config.js");

module.exports = merge(commonConfig, {
  mode: "development",
  devtool: "eval-cheap-source-map"
});