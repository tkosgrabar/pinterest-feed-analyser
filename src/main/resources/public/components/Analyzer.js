var PinTable = React.createClass({
    render: function () {
        var rows = this.props.data.map(function (analysis) {
            return (
                <Pin count={analysis.count} pin={analysis.pin}/>
            );
        });

        return (
            <ReactBootstrap.Table responsive>
                <thead>
                <tr>
                    <th>Count</th>
                    <th>Image</th>
                    <th>Description</th>
                    <th>Pin url</th>
                </tr>
                </thead>
                <tbody>
                {rows}
                </tbody>
            </ReactBootstrap.Table>
        );
    }
});

var Pin = React.createClass({
    render: function () {
        return (
            <tr>
                <td>
                    {this.props.count}
                </td>
                <td>
                    <img src={this.props.pin.imgUrl}/>
                </td>
                <td>
                    {this.props.pin.description}
                </td>
                <td>
                    {this.props.pin.pinUrl}
                </td>
            </tr>
        );
    }
});


var Analyzer = React.createClass({
    getInitialState: function () {
        return {
            disabled: false,
            url: '',
            count: '',
            analysis: []
        };
    },
    handleUrlChanged: function (e) {
        this.setState({url: e.target.value});
    },
    handleCountChanged: function (e) {
        this.setState({count: e.target.value});
    },
    handleSubmit: function (e) {
        e.preventDefault();

        var url = this.state.url.trim();
        var count = this.state.count;
        if (!url || !count) {
            return;
        }
        this.setState({disabled: true});
        var request = {
            url: url,
            count: count
        };
        $.ajax({
            url: this.props.url,
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify(request),
            success: function (data) {
                this.setState({
                        analysis: data.analysis,
                        disabled: false,
                        url: '',
                        count: ''
                    }
                );
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(this.props.url, status, err.toString());
                this.setState({
                        analysis: [],
                        disabled: false,
                        url: '',
                        count: ''
                    }
                );
            }.bind(this)
        });
    },
    render: function () {
        return (
            <div>
                <ReactBootstrap.Row className="show-grid">
                    <ReactBootstrap.Col xs={12} md={8} xsOffset={1}>
                        <ReactBootstrap.Form inline onSubmit={this.handleSubmit}>
                            <ReactBootstrap.FormGroup controlId="formInlineName">
                                <ReactBootstrap.ControlLabel>Url</ReactBootstrap.ControlLabel>
                                {' '}
                                <ReactBootstrap.FormControl type="text" placeholder="Pinterest url to analyze"
                                                            value={this.state.url}
                                                            disabled={this.state.disabled}
                                                            onChange={this.handleUrlChanged}/>
                            </ReactBootstrap.FormGroup>
                            {' '}
                            <ReactBootstrap.FormGroup controlId="formInlineEmail">
                                <ReactBootstrap.ControlLabel>Count</ReactBootstrap.ControlLabel>
                                {' '}
                                <ReactBootstrap.FormControl type="number" placeholder="Pins count"
                                                            value={this.state.count}
                                                            disabled={this.state.disabled}
                                                            onChange={this.handleCountChanged}/>
                            </ReactBootstrap.FormGroup>
                            {' '}
                            <ReactBootstrap.Button type="submit" disabled={this.state.disabled}>
                                Analyze
                            </ReactBootstrap.Button>
                        </ReactBootstrap.Form>
                    </ReactBootstrap.Col>
                </ReactBootstrap.Row>
                <br/>
                <ReactBootstrap.Row className="show-grid">
                    <ReactBootstrap.Col xs={6} md={6} xsOffset={1}>
                        <PinTable data={this.state.analysis}/>
                    </ReactBootstrap.Col>
                </ReactBootstrap.Row>
            </div>
        );
    }
});

ReactDOM.render(
    <Analyzer url="/api/analyze"/>,
    document.getElementById('content')
);