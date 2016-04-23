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
                    <img src={this.props.pin.imgUrl} style={{maxHeight : 500 + 'px', maxWidth: 500 + 'px'}}/>
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
            filter: '',
            analysis: []
        };
    },
    handleUrlChanged: function (e) {
        this.setState({url: e.target.value});
    },
    handleCountChanged: function (e) {
        this.setState({count: e.target.value});
    },
    handleFilterChanged: function (e) {
        this.setState({filter: e.target.value});
    },
    handleSubmit: function (e) {
        e.preventDefault();

        var url = this.state.url.trim();
        var count = this.state.count;
        var filter = this.state.filter.trim();
        if (!url || !count) {
            return;
        }
        this.setState({disabled: true});
        var request = {
            url: url,
            count: count,
            filter: filter
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
                        count: '',
                        filter: ''
                    }
                );
            }.bind(this),
            error: function (xhr, status, err) {
                console.error(this.props.url, status, err.toString());
                this.setState({
                        analysis: [],
                        disabled: false,
                        url: '',
                        count: '',
                        filter: ''
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
                            <ReactBootstrap.FormGroup>
                                <ReactBootstrap.ControlLabel>Url</ReactBootstrap.ControlLabel>
                                {' '}
                                <ReactBootstrap.FormControl type="text" placeholder="Pinterest url to analyze"
                                                            value={this.state.url}
                                                            disabled={this.state.disabled}
                                                            onChange={this.handleUrlChanged}/>
                            </ReactBootstrap.FormGroup>
                            {' '}
                            <ReactBootstrap.FormGroup>
                                <ReactBootstrap.ControlLabel>Count</ReactBootstrap.ControlLabel>
                                {' '}
                                <ReactBootstrap.FormControl type="number" placeholder="Pins count"
                                                            value={this.state.count}
                                                            disabled={this.state.disabled}
                                                            onChange={this.handleCountChanged}/>
                            </ReactBootstrap.FormGroup>
                            {' '}
                            <ReactBootstrap.FormGroup>
                                <ReactBootstrap.ControlLabel>Filter</ReactBootstrap.ControlLabel>
                                {' '}
                                <ReactBootstrap.FormControl type="text" placeholder="Filter description"
                                                            value={this.state.filter}
                                                            disabled={this.state.disabled}
                                                            onChange={this.handleFilterChanged}/>
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