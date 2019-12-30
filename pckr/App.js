import React from 'react';
import PickerModule from 'react-native-picker-module';
import {
    View,
    Text,
    Image,
    Button,
} from 'react-native';
import {
    MediaTypeFile,
    MediaTypePhoto,
    MediaTypeVideo,
    PHOTO_COVER,
    PHOTO_PROFILE,
    POST_TALL,
    POST_WIDE
} from "./src/MediaPickOptions";

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
                    <Separator/>
                    <Text>Uri: {JSON.stringify(this.state.pickResponse)}</Text>
                    <Separator/>
                    <Button onPress={() => {
                        PickerModule.pickMedia({
                            mediaType: MediaTypePhoto,
                            proportion: PHOTO_PROFILE,
                            nextButtonString: "Next"
                        }).then((result) => {
                            this.setState({pickResponse: result});
                        }).catch((error) => {
                            console.log(error);
                        })
                    }
                    } title="Pick Square Image"/>
                    <Separator/>
                    <Button onPress={() => {
                        PickerModule.pickMedia({
                            mediaType: MediaTypePhoto,
                            proportion: PHOTO_COVER,
                        }).then((result) => {
                            this.setState({pickResponse: result});
                        }).catch((error) => {
                            console.log(error);
                        })
                    }
                    } title="Pick Cover Image"/>
                    <Separator/>
                    <Button onPress={() => {
                        PickerModule.pickMedia({
                            mediaType: MediaTypePhoto,
                            proportion: POST_TALL,
                        }).then((result) => {
                            this.setState({pickResponse: result});
                        }).catch((error) => {
                            console.log(error);
                        })
                    }
                    } title="Pick Tall Image"/>
                    <Separator/>
                    <Button onPress={() => {
                        PickerModule.pickMedia({
                            mediaType: MediaTypePhoto,
                            proportion: POST_WIDE,
                        }).then((result) => {
                            this.setState({pickResponse: result});
                        }).catch((error) => {
                            console.log(error);
                        })
                    }
                    } title="Pick Wide Image"/>
                    <Separator/>
                    <Button onPress={() => {
                        PickerModule.pickMedia({
                            mediaType: MediaTypeVideo,
                        }).then((result) => {
                            this.setState({pickResponse: result});
                        }).catch((error) => {
                            console.log(error);
                        })
                    }
                    } title="Pick Video"/>
                    <Separator/>
                    <Button onPress={() => {
                        PickerModule.pickMedia({
                            mediaType: MediaTypeFile,
                        }).then((result) => {
                            this.setState({pickResponse: result});
                        }).catch((error) => {
                            console.log(error);
                        })
                    }
                    } title="Pick File"/>
                    <Separator/>
                </View>
            </View>
        );
    }
}
