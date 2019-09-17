/* eslint-disable */
const path = require("path");
const glob = require('glob');

const CopyWebpackPlugin = require('copy-webpack-plugin');
const  { CleanWebpackPlugin } = require('clean-webpack-plugin');

module.exports = {
  entry: getAllFiles('src/**/*.js'), // questo meccanismo server per creare file sperati 
                                      //come output in congiunzione con [name]
  output: {
      path: path.resolve(__dirname, "dist"),
      filename: "[name]"
    },
    module: {
      rules: [
        {
          test: /\.js$/,
          exclude: /(node_modules)/,
          use: {
            loader: "babel-loader"
          }
        }
      ]
    },
    plugins: [
      new CleanWebpackPlugin(),
      new CopyWebpackPlugin([{
        from: __dirname + '/src',
        to: __dirname + '/dist'
       }])
    ],
    devServer: {
        contentBase: path.join(__dirname, 'dist'),
        compress: true,
        writeToDisk:true, // necessario per riscrivere i file dopo il clean
        port: 9000
    }
  };

  function getAllFiles(pattern) {
    const entries = {};
  
    glob.sync(pattern).forEach((file) => {
      entries[file.replace('src/', '')] = path.join(__dirname, file);
    });

      return entries;
  }