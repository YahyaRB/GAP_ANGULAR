import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteProjetComponent } from './delete-projet.component';

describe('DeleteProjetComponent', () => {
  let component: DeleteProjetComponent;
  let fixture: ComponentFixture<DeleteProjetComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DeleteProjetComponent]
    });
    fixture = TestBed.createComponent(DeleteProjetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
