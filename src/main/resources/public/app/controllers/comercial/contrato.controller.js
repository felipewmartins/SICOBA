(function () {
    'use strict';

    angular.module('sicobaApp')
        .controller('ContratoCtrl', function ($scope, $rootScope, $routeParams, Cliente, Contrato, Plano, Equipamento) {

            $scope.save = _save;
            $scope.remove = _remove;

            _init();

            function _init() {
                $scope.clienteId = $routeParams.clienteId;
                $scope.hoje = new Date();
                $scope.planos = Plano.query();
                $scope.equipamentosInstalacao = Equipamento.disponiveisParaInstalacao();
                $scope.equipamentosWifi = Equipamento.disponiveisParaWifi();
                _carregarContrato();
            }

            function _carregarContrato() {
                Contrato.buscarPorCliente({clienteId: $routeParams.clienteId}, function (contrato) {
                    if (contrato.id) {
                        $scope.contrato = contrato;
                        if (contrato.equipamento) {
                            $scope.equipamentosInstalacao.push(contrato.equipamento);
                        }
                        if (contrato.equipamentoWifi) {
                            $scope.equipamentosWifi.push(contrato.equipamentoWifi);
                        }
                    } else {
                        $scope.contrato = {
                            cliente: Cliente.get({id: $routeParams.clienteId})
                        };
                    }
                });
            }

            function _save(contrato) {
                Contrato.save(contrato, function (data) {
                    $scope.contrato = data;
                    $rootScope.messages = [{
                        title: 'Sucesso:',
                        body: 'Contrato de número ' + data.id + ' foi salvo.',
                        type: 'alert-success'
                    }];
                });
            }

            function _remove(contrato) {
                Contrato.remove({id: contrato.id}, function (data) {
                    _init();
                    $rootScope.messages = [{
                        title: 'Sucesso:',
                        body: 'Contrato de número ' + contrato.id + ' foi removido.',
                        type: 'alert-success'
                    }];
                });
            }
        });
}());
