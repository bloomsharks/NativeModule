import React from 'react';
import PickerModule from 'react-native-picker-module';
import {View, Text, Image, Button, StatusBar} from 'react-native';
import SplashScreen from 'react-native-splash-screen';

function Separator() {
  return (
    <View
      style={{
        marginVertical: 8,
      }}
    />
  );
}

export default class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      pickResponse: {},
    };
  }

  componentDidMount() {
    // do stuff while splash screen is shown
      // After having done stuff (such as async tasks) hide the splash screen
      setTimeout(function(){
        SplashScreen.hide();
      }, 700)
  }

  render() {
    return (
      <View
        style={{
          backgroundColor: 'white',
          width: '100%',
          height: '100%',
        }}>
        <StatusBar 
          backgroundColor="#000"
          barStyle="light-content"
          />
        <View
          style={{
            marginTop: 32,
            paddingHorizontal: 24,
          }}>
          <Separator />
          <Text>Result: {JSON.stringify(this.state.pickResponse)}</Text>
          <Separator />
          <Button
            onPress={() => {
              PickerModule.pickMedia({
                mediaType: 'photo',
                skipCrop: true,
              })
                .then(result => {
                  this.setState({pickResponse: result});
                })
                .catch(error => {
                  this.setState({
                    pickResponse: {
                      code: error.code,
                      message: error.message,
                    },
                  });
                });
            }}
            title="Just Pick Image"
          />
          <Separator />
          <Button
            onPress={() => {
              PickerModule.pickMedia({
                mediaType: 'photo',
                proportion: 'profile',
              })
                .then(result => {
                  this.setState({pickResponse: result});
                })
                .catch(error => {
                  this.setState({
                    pickResponse: {
                      code: error.code,
                      message: error.message,
                    },
                  });
                });
            }}
            title="Pick Square Image"
          />
          <Separator />
          <Button
            onPress={() => {
              PickerModule.pickMedia({
                mediaType: 'photo',
                proportion: 'cover',
              })
                .then(result => {
                  this.setState({pickResponse: result});
                })
                .catch(error => {
                  this.setState({
                    pickResponse: {
                      code: error.code,
                      message: error.message,
                    },
                  });
                });
            }}
            title="Pick Cover Image"
          />
          <Separator />
          <Button
            onPress={() => {
              PickerModule.pickMedia({
                mediaType: 'photo',
                proportion: 'post',
              })
                .then(result => {
                  this.setState({pickResponse: result});
                })
                .catch(error => {
                  this.setState({
                    pickResponse: {
                      code: error.code,
                      message: error.message,
                    },
                  });
                });
            }}
            title="Pick Post Image"
          />
          <Separator />
          <Button
            onPress={() => {
              PickerModule.pickMedia({
                mediaType: 'photo',
                ratioX: 37.0,
                ratioY: 14.0,
              })
                .then(result => {
                  this.setState({pickResponse: result});
                })
                .catch(error => {
                  this.setState({
                    pickResponse: {
                      code: error.code,
                      message: error.message,
                    },
                  });
                });
            }}
            title="Pick Custom Image"
          />
          <Separator />
          <Button
            onPress={() => {
              PickerModule.pickMedia({
                mediaType: 'video',
                doTrim: true,
                compressAfterTrim: false,
                doEncode: false,
                minSeconds: 1,
                maxSeconds: 90,
                staticText: 'Hello From RN',
                maxDisplayedThumbs: 3,
              })
                .then(result => {
                  this.setState({pickResponse: result});
                })
                .catch(error => {
                  this.setState({
                    pickResponse: {
                      code: error.code,
                      message: error.message,
                    },
                  });
                });
            }}
            title="Pick Video"
          />
          <Separator />
          <Button
            onPress={() => {
              PickerModule.pickMedia({
                mediaType: 'file',
              })
                .then(result => {
                  this.setState({pickResponse: result});
                })
                .catch(error => {
                  this.setState({
                    pickResponse: {
                      code: error.code,
                      message: error.message,
                    },
                  });
                });
            }}
            title="Pick File"
          />
          <Separator />
        </View>
      </View>
    );
  }
}
