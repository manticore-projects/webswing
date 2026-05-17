const { merge } = require("webpack-merge");
const commonConfig = require("./webpack.config.js");
const path = require("path");
const DESTINATION = path.resolve(__dirname, "../webswing-server-war/target/webswing-server/javascript");

module.exports = merge(commonConfig, {
    mode: "development",
    devtool: "cheap-source-map",
    output: {
        path: DESTINATION
    }
});