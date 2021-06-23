Confluent For Kubernetes today provides Custom Resource Definitions(CRDs) which are Kuberetes Operators to deploy Confluent Server a.k.a Confluent Platform(CP) 6.2

Earlier versions of Confluent Operator - anything 1.x came with Helm Charts to deploy CP - until v6.1

We would have existing customers that have used Terraform(TF) provider to deploy these helm charts to deploy CP against a Kubernetes cluster. For such customers, using TF with CRD is currently not possible because the [provider](https://github.com/hashicorp/terraform-provider-kubernetes-alpha) is not production ready.

Hence, in order to deploy CP keeping the templating tool same i.e TF, we are able to use the CRDs with TF provider [kustomization](https://registry.terraform.io/providers/kbst/kustomize/latest/docs) to deploy to the Kubernetes(K8s) cluster.

In this example, I'll be using the Terraform provider kustomization to achieve this.

Pre-requisites for doing this:
1. TF
2. kubectl
3. K8s cluster (I spun up a GKE cluster using the sample [TF project](https://github.com/hashicorp/learn-terraform-provision-gke-cluster)

Steps:
1. Following the Quickstart demo documented [here](https://github.com/confluentinc/confluent-kubernetes-examples/tree/master/quickstart-deploy), ran below commands:
    ```
    helm repo add confluentinc https://packages.confluent.io/helm
    helm upgrade --install operator confluentinc/confluent-for-kubernetes -n confluent

2. The above code deploys CFK in the confluent namespace
3. The quickstart also gives away a big yaml [file](https://github.com/confluentinc/confluent-kubernetes-examples/blob/master/quickstart-deploy/confluent-platform.yaml) that defines the whole Confluent cluster. I split this up into smaller files for readability and granularity. This makes up for one yaml file each for each Confluent Platform component a.k.a zookeeper, kafka, schema registry etc.
4. The kustomization.yaml file in this repo is a collection of what CRDs you want to deploy as part of your CP deployment
4. Run the below commands to deploy your CRDs on the K8s cluster using TF
``` 
    rsubramanian@C02D % terraform init
    rsubramanian@C02D % terraform apply
```
5. Test if all components are deployed:
    ```
    rsubramanian@C02D % kubectl get all -n confluent
        NAME                                      READY   STATUS    RESTARTS   AGE
        pod/confluent-operator-786d685f69-xxvt6   1/1     Running   0          32m
        pod/connect-0                             1/1     Running   1          29m
        pod/controlcenter-0                       1/1     Running   0          26m
        pod/kafka-0                               1/1     Running   0          27m
        pod/kafka-1                               1/1     Running   0          27m
        pod/kafka-2                               1/1     Running   0          27m
        pod/ksqldb-0                              1/1     Running   0          26m
        pod/schemaregistry-0                      1/1     Running   0          26m
        pod/zookeeper-0                           1/1     Running   0          29m
        pod/zookeeper-1                           1/1     Running   0          29m
        pod/zookeeper-2                           1/1     Running   0          29m

        NAME                                TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)                                                          AGE
        service/confluent-operator          ClusterIP   10.151.243.16    <none>        7778/TCP                                                         32m
        service/connect                     ClusterIP   None             <none>        8083/TCP,7203/TCP,7777/TCP,7778/TCP                              29m
        service/connect-0-internal          ClusterIP   10.151.245.143   <none>        8083/TCP,7203/TCP,7777/TCP,7778/TCP                              29m
        service/controlcenter               ClusterIP   None             <none>        9021/TCP,7203/TCP,7777/TCP,7778/TCP                              26m
        service/controlcenter-0-internal    ClusterIP   10.151.249.116   <none>        9021/TCP,7203/TCP,7777/TCP,7778/TCP                              26m
        service/kafka                       ClusterIP   None             <none>        9092/TCP,8090/TCP,9071/TCP,7203/TCP,7777/TCP,7778/TCP,9072/TCP   27m
        service/kafka-0-internal            ClusterIP   10.151.250.125   <none>        9092/TCP,8090/TCP,9071/TCP,7203/TCP,7777/TCP,7778/TCP,9072/TCP   27m
        service/kafka-1-internal            ClusterIP   10.151.251.144   <none>        9092/TCP,8090/TCP,9071/TCP,7203/TCP,7777/TCP,7778/TCP,9072/TCP   27m
        service/kafka-2-internal            ClusterIP   10.151.247.221   <none>        9092/TCP,8090/TCP,9071/TCP,7203/TCP,7777/TCP,7778/TCP,9072/TCP   27m
        service/ksqldb                      ClusterIP   None             <none>        8088/TCP,7203/TCP,7777/TCP,7778/TCP                              26m
        service/ksqldb-0-internal           ClusterIP   10.151.240.196   <none>        8088/TCP,7203/TCP,7777/TCP,7778/TCP                              26m
        service/schemaregistry              ClusterIP   None             <none>        8081/TCP,7203/TCP,7777/TCP,7778/TCP                              26m
        service/schemaregistry-0-internal   ClusterIP   10.151.254.43    <none>        8081/TCP,7203/TCP,7777/TCP,7778/TCP                              26m
        service/zookeeper                   ClusterIP   None             <none>        2181/TCP,7203/TCP,7777/TCP,3888/TCP,2888/TCP,7778/TCP            29m
        service/zookeeper-0-internal        ClusterIP   10.151.250.197   <none>        2181/TCP,7203/TCP,7777/TCP,3888/TCP,2888/TCP,7778/TCP            29m
        service/zookeeper-1-internal        ClusterIP   10.151.250.76    <none>        2181/TCP,7203/TCP,7777/TCP,3888/TCP,2888/TCP,7778/TCP            29m
        service/zookeeper-2-internal        ClusterIP   10.151.249.218   <none>        2181/TCP,7203/TCP,7777/TCP,3888/TCP,2888/TCP,7778/TCP            29m

        NAME                                 READY   UP-TO-DATE   AVAILABLE   AGE
        deployment.apps/confluent-operator   1/1     1            1           32m

        NAME                                            DESIRED   CURRENT   READY   AGE
        replicaset.apps/confluent-operator-786d685f69   1         1         1       32m

        NAME                              READY   AGE
        statefulset.apps/connect          1/1     29m
        statefulset.apps/controlcenter    1/1     26m
        statefulset.apps/kafka            3/3     27m
        statefulset.apps/ksqldb           1/1     26m
        statefulset.apps/schemaregistry   1/1     26m
        statefulset.apps/zookeeper        3/3     29m

        NAME                                    REPLICAS   READY   STATUS    AGE
        connect.platform.confluent.io/connect   1          1       RUNNING   29m

        NAME                                  REPLICAS   READY   STATUS    AGE
        ksqldb.platform.confluent.io/ksqldb   1          1       RUNNING   29m

        NAME                                                REPLICAS   READY   STATUS    AGE
        controlcenter.platform.confluent.io/controlcenter   1          1       RUNNING   29m

        NAME                                REPLICAS   READY   STATUS    AGE
        kafka.platform.confluent.io/kafka   3          3       RUNNING   29m

        NAME                                        REPLICAS   READY   STATUS    AGE
        zookeeper.platform.confluent.io/zookeeper   3          3       RUNNING   29m

        NAME                                                  REPLICAS   READY   STATUS    AGE
        schemaregistry.platform.confluent.io/schemaregistry   1          1       RUNNING   29m
    ```
6. Now, do port forwarding to access Control Center:
    ```
    rsubramanian@C02D  % kubectl port-forward pod/controlcenter-0 9021:9021 -n confluent
        Forwarding from 127.0.0.1:9021 -> 9021
        Forwarding from [::1]:9021 -> 9021
        Handling connection for 9021
        Handling connection for 9021
        Handling connection for 9021
        Handling connection for 9021
        Handling connection for 9021
    ```
7. Verify if Control Center is accessible:
    ```
    rsubramanian@C02D % curl http://localhost:9021
    ```
