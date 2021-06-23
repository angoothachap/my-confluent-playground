terraform {
required_providers {
kustomization = {
source = "kbst/kustomization"
version = "0.5.0"
}
}
}
provider "kubernetes" {
config_path= "~/.kube/config"
config_context = "gke_rema-subramanian-lab_us-central1_rema-subramanian-lab-gke"
}

provider "kustomization" {
kubeconfig_path = "~/.kube/config"
}
data "kustomization_build" "test" {
path = "test_kustomizations/resources"
}

resource "kustomization_resource" "test" {
for_each = data.kustomization_build.test.ids

manifest = data.kustomization_build.test.manifests[each.value]
}
