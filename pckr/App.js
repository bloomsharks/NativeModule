import React from 'react';
import PickerModule from 'react-native-picker-module';
import {View, Text, Image, Button} from 'react-native';

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

  render() {
    return (
      <View
        style={{
          width: '100%',
          height: '100%',
        }}>
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
                maxFileSizeBytes: 10000,
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
                proportion: 'custom',
                keyX: '37.0',
                keyY: '14.0',
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
